package com.example.listener;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 用于处理邮件发送的消息队列监听器
 */
@Component
@RabbitListener(queues = "mail.queue")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /**
     * 处理邮件发送
     * @param dataStr JSON 格式的邮件信息
     */
    @RabbitHandler
    public void sendMailMessage(String dataStr) {
        JSONObject data = JSONObject.parseObject(dataStr);
        String email = data.getString("email");
        Integer code = data.getInteger("code");
        SimpleMailMessage message = switch (data.getString("type")) {
            case "register" ->
                    createMessage("欢迎注册我们的网站",
                            "您的邮件注册验证码为: " + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息。",
                            email);
            case "reset" ->
                    createMessage("您的密码重置邮件",
                            "你好，您正在执行重置密码操作，验证码: " + code + "，有效时间3分钟，如非本人操作，请无视。",
                            email);
            default -> null;
        };
        if (message == null) return;
        sender.send(message);
    }

    /**
     * 快速封装简单邮件消息实体
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 邮件实体
     */
    private SimpleMailMessage createMessage(String title, String content, String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        // 如果配置了发件人就用配置的，否则用一个默认地址（开发环境 Mailpit 不需要真实邮箱）
        message.setFrom(username == null || username.isBlank() ? "noreply@localhost" : username);
        return message;
    }
}

/*
 * ══════════════════════════════════════════════════════════════════════════════
 *  MailQueueListener 的运行机制
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * ─── 先搞懂三个角色 ───
 *
 *   整个系统里有三个独立的程序在协同工作：
 *
 *   ╔══════════════════════════════════════════════════════════════╗
 *   ║                  你的 Spring Boot 应用                      ║
 *   ║  ┌──────────────────────┐    ┌──────────────────────────┐   ║
 *   ║  │  AccountService      │    │  MailQueueListener       │   ║
 *   ║  │  (Java 代码)         │    │  (也是 Java 代码)        │   ║
 *   ║  │                      │    │                          │   ║
 *   ║  │  发消息 →            │    │  后台线程连接 RabbitMQ   │   ║
 *   ║  └─────────┬────────────┘    └───────────┬──────────────┘   ║
 *   ╚════════════╪══════════════════════════════╪═════════════════╝
 *                │ 网络连接（TCP/IP）            │ 网络连接（TCP/IP）
 *                ▼                              ▼
 *   ╔══════════════════════════════════════════════════════════════╗
 *   ║               RabbitMQ 服务器（独立进程）                    ║
 *   ║                                                              ║
 *   ║  里面有一个队列叫 "mail.queue"                               ║
 *   ║  消息就是存在这里面的                                        ║
 *   ╚══════════════════════════════════════════════════════════════╝
 *
 * ─── RabbitConfiguration 配置的队列到底是什么？ ───
 *
 *   @Bean("mailQueue")
 *   public Queue queue(){
 *       return QueueBuilder.durable("mail.queue").build();
 *   }
 *
 *   这段代码的意思是：在项目启动时，通过网络告诉 RabbitMQ 服务器：
 *   "请在服务器上创建一个叫 mail.queue 的队列"。这个队列不是 Java
 *   内存里的对象，而是真正存在于 RabbitMQ 服务器进程里的一个数据结构。
 *
 *   你可以打开 http://localhost:15672 → Queues 标签，看到它。
 *   就像 MySQL 里 CREATE TABLE 是在数据库服务器上创建一张表一样。
 *
 * ─── "发消息"到底是怎么发的？ ───
 *
 *   AccountService 里的这行代码：
 *
 *     rabbitTemplate.convertAndSend("mail.queue", jsonString);
 *
 *   背后做的事情：
 *
 *   ① RabbitTemplate 把 jsonString 打包成 RabbitMQ 协议的消息格式
 *      ↓
 *   ② 通过 TCP 连接（localhost:5672）发送到 RabbitMQ 服务器
 *      ↓
 *   ③ RabbitMQ 服务器收到消息，把它存到 "mail.queue" 队列里
 *      ↓
 *   ④ convertAndSend 返回（整个过程 1 毫秒左右）
 *
 *   消息现在就在 RabbitMQ 服务器里排队等着。如果没人消费，
 *   它会一直存在（因为队列是 durable 的，重启也不丢）。
 *
 * ─── "监听"到底是怎么监听的？ ───
 *
 *   这里的关键认知是：不是 RabbitMQ 主动通知我们，而是我们
 *   自己开着连接一直在问。
 *
 *   @RabbitListener(queues = "mail.queue")
 *   public class MailQueueListener { ... }
 *
 *   Spring Boot 启动时看到这个注解，会做：
 *
 *   ① 建立一个 TCP 长连接（一直连着不断）到 RabbitMQ 服务器（:5672）
 *      ↓
 *   ② 向 RabbitMQ 注册一个"消费者"：我对 mail.queue 感兴趣
 *      ↓
 *   ③ 启动一个后台线程，在这个连接上"阻塞等待"
 *      ↓
 *   ④ 当 RabbitMQ 服务器有消息进入 mail.queue 时，它会把消息
 *      "推送"给这个后台线程
 *      ↓
 *   ⑤ 后台线程收到消息 → 交给 SimpleMessageConverter 转成 String
 *      ↓
 *   ⑥ 调用 @RabbitHandler 标注的 sendMailMessage(dataStr)
 *      ↓
 *   ⑦ 方法执行完 → 后台线程告诉 RabbitMQ "我处理完了，可以删了"
 *      ↓
 *   ⑧ 线程回到步骤 ③，继续阻塞等待下一条消息
 *
 *   这个过程类似：
 *     - 你打电话给客服（建立长连接）
 *     - 你说"我等着，有消息就告诉我"（注册监听）
 *     - 你拿着电话不挂断（阻塞等待）
 *     - 客服有新消息时告诉你（服务器推送）
 *
 *   而不是：你每隔 1 秒打一次电话问"有消息了吗"（轮询）。
 *
 * ─── 配置里的队列和监听的关系 ───
 *
 *   RabbitConfiguration             MailQueueListener
 *   ──────────────────               ─────────────────
 *   "帮我创建队列"                     "我要监听这个队列"
 *         │                                  │
 *         ▼                                  ▼
 *   RabbitMQ 服务器上有一个 mail.queue
 *         │
 *         ▼
 *   AccountService 发消息到这里
 *
 *   三个地方都要用同一个队列名 "mail.queue"，否则对不上：
 *   - RabbitConfiguration 创建的是 "mail.queue"
 *   - AccountService 发往的是 "mail.queue"
 *   - MailQueueListener 监听的是 "mail.queue"
 *
 * ─── 消息队列 vs 传统调用的本质区别 ───
 *
 *   传统方式（同步）：
 *     AccountService.send(email, code) → 直接调 JavaMailSender
 *       → 连接 SMTP 服务器（3 秒）
 *       → 返回
 *     用户等 3 秒才收到响应
 *
 *   消息队列方式（异步）：
 *     AccountService → rabbitTemplate.convertAndSend() → 1 毫秒返回
 *                                                       用户立即收到"已发送"
 *                                                           │
 *     MailQueueListener（在后台另一个线程里）←───────────────┘
 *       → 连接 SMTP 服务器（3 秒）
 *       → 邮件发出
 *     用户不需要等这 3 秒
 *
 * ─── 这个类的角色定位 ───
 *
 *   ┌──────────────────────────────────────────────────────────┐
 *   │  你的 Spring Boot 应用（一个进程）                    │
 *   │                                                      │
 *   │  ┌─────────────────┐      ┌──────────────────────┐   │
 *   │  │ AccountService  │      │  MailQueueListener   │   │
 *   │  │ （主线程）       │      │  （后台线程）         │   │
 *   │  │                 │      │                      │   │
 *   │  │ 发消息到队列     │ ──►  │ 一直连在 RabbitMQ 上 │   │
 *   │  │ 1ms 返回        │      │ 收到消息 → 发邮件     │   │
 *   │  └─────────────────┘      └──────────────────────┘   │
 *   │              │                      │                │
 *   └──────────────┼──────────────────────┼────────────────┘
 *                  ▼                      ▼
 *          ┌──────────────┐      ┌──────────────┐
 *          │  RabbitMQ    │      │  Mailpit     │
 *          │  服务器       │      │  SMTP 服务器  │
 *          │  localhost:  │      │  localhost:   │
 *          │  5672        │      │  1025         │
 *          └──────────────┘      └──────────────┘
 *
 * ══════════════════════════════════════════════════════════════════════════════
 *  附：为什么 RabbitMQ 一直报 Failed to convert message？
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  根本原因：SimpleMessageConverter 默认用 Java 序列化，而 Spring AMQP 4.x
 *  有严格的反序列化白名单，HashMap、CollSer 等常用类都不在其中。
 *
 *         发送 HashMap → Java 序列化（ObjectOutputStream）
 *                      → 内容类型 application/x-java-serialized-object
 *                      → 接收时反序列化 → 白名单检查 → SecurityException ❌
 *
 *         发送 String  → 直接写字节数组
 *                      → 内容类型 text/plain
 *                      → 接收时直接 new String(bytes) → 无白名单检查 ✅
 *
 *  解决：把 AccountService 发送的内容从 HashMap 改为 JSON 字符串，
 *        MailQueueListener 对应改为收 String 再用 FastJSON2 解析。
 *        这样全程不走 Java 序列化，白名单形同虚设。
 */
