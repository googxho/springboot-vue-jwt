package com.example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置
 */
@Configuration
public class RabbitConfiguration {

    @Bean("mailQueue")
    public Queue queue(){
        return QueueBuilder
                .durable("mail.queue")
                .build();
    }
}

/*
 * ══════════════════════════════════════════════════════════════════════════
 *  RabbitMQ 是什么？它在我的项目里扮演什么角色？
 * ══════════════════════════════════════════════════════════════════════════
 *
 *  ▸  RabbitMQ 不是插件，不是 Java 库，它是一个独立的服务器程序。
 *  ▸  它和 MySQL、Redis 是同一类东西——都需要你额外安装、启动、维护。
 *  ▸  你的 Java 代码通过网络连接到 RabbitMQ 服务器，跟连 MySQL 一样。
 *
 * ─── 整个项目的架构长这样 ───
 *
 *   ╔══════════════════════════════════════════════════════╗
 *   ║              你的电脑（localhost）                    ║
 *   ║                                                      ║
 *   ║  ┌─────────────────────┐                            ║
 *   ║  │  Spring Boot 项目   │    连接                    ║
 *   ║  │  (你的 Java 代码)   │────→  MySQL（端口 3306）    ║
 *   ║  │                    │────→  Redis（端口 6379）     ║
 *   ║  │                    │────→  RabbitMQ（端口 5672）  ║
 *   ║  └─────────────────────┘                            ║
 *   ║       │         ↑                                   ║
 *   ║       │         └── MailQueueListener 从队列取消息   ║
 *   ║       │            然后调用 JavaMailSender 发邮件    ║
 *   ║       ▼                                             ║
 *   ║  AccountService 把消息丢进 RabbitMQ 队列             ║
 *   ║  （几乎不花时间，立即返回给用户）                      ║
 *   ╚══════════════════════════════════════════════════════╝
 *
 * ─── 三个"服务器程序"在你项目中的角色对比 ───
 *
 *   ┌──────────┬───────────────┬──────────────────┬─────────────┐
 *   │ 名称     │ 它是干什么的  │ 默认端口         │ 怎么安装    │
 *   ├──────────┼───────────────┼──────────────────┼─────────────┤
 *   │ MySQL    │ 存数据        │ 3306             │ brew install│
 *   │          │（用户信息等）  │                  │ mysql       │
 *   ├──────────┼───────────────┼──────────────────┼─────────────┤
 *   │ Redis    │ 内存缓存      │ 6379             │ brew install│
 *   │          │（验证码、JWT） │                  │ redis       │
 *   ├──────────┼───────────────┼──────────────────┼─────────────┤
 *   │ RabbitMQ │ 消息队列      │ 5672（通信）     │ brew install│
 *   │          │（异步发邮件）  │ 15672（管理页面） │ rabbitmq    │
 *   └──────────┴───────────────┴──────────────────┴─────────────┘
 *
 * ─── RabbitMQ 的工作方式（逐步拆解） ───
 *
 *   第 1 层：RabbitMQ 服务器本身
 *     - 它是一个用 Erlang 语言写的服务器程序
 *     - 安装后用 brew services start rabbitmq 启动
 *     - 启动后它就一直在后台运行，等待 Java 代码来连接它
 *     - 它还自带一个网页管理界面：http://localhost:15672（可查看队列状态）
 *
 *   第 2 层：Java 和 RabbitMQ 之间的"桥梁"
 *     - 就是 pom.xml 里加的 spring-boot-starter-amqp 这个依赖
 *     - 这个依赖里面包含了"Java 连接 RabbitMQ 服务器的驱动程序"
 *     - 它提供了 AmqpTemplate（发消息）和 @RabbitListener（收消息）
 *
 *   第 3 层：配置类（就是这个文件）
 *     - 告诉 RabbitMQ 服务器："帮我创建一个叫 mail 的队列"
 *     - 相当于在 MySQL 里 CREATE TABLE 一样，得先有"容器"才能放东西
 *
 * ─── 整个流程走一遍 ───
 *
 *   ① 用户请求验证码 → GET /api/auth/ask-code?email=xxx
 *   ② AccountService.registerEmailVerifyCode()
 *      ├─ 生成 6 位随机码：123456
 *      ├─ 拼装消息：{type:"register", email:"xxx", code:123456}
 *      ├─ rabbitTemplate.convertAndSend("mail", 消息)
 *      │    └─ 这行代码通过网络把消息发到 RabbitMQ 服务器
 *      │       RabbitMQ 收到后存到 "mail" 队列里
 *      └─ 立即返回"已发送"给用户 ← 这里不等待发邮件！
 *
 *   ③ RabbitMQ 服务器里的 "mail" 队列中多了一条消息在排队
 *
 *   ④ MailQueueListener（一直在后台监听）感知到有新消息
 *      ├─ 从队列取出消息
 *      ├─ 根据 type 选择邮件模板
 *      └─ JavaMailSender.send() → 真正通过 SMTP 发邮件
 *
 * ─── 为什么这样设计的核心原因 ───
 *
 *   发邮件需要通过 SMTP 协议连接到 163 邮箱的服务器（smtp.163.com），
 *   这是一个网络请求，通常耗时 1~3 秒。
 *
 *   如果不用消息队列，用户请求验证码时就要原地等 3 秒才能收到响应。
 *   如果用消息队列，1 毫秒就把任务丢进队列了，用户立即收到"已发送"。
 *
 *   这就叫"异步"——先把结果告诉用户，后台慢慢处理耗时任务。
 *
 * ─── 你可能会问：那没有 RabbitMQ 能不能发？ ───
 *
 *   能。不用 RabbitMQ 的话，直接在 AccountService 里调用
 *   JavaMailSender.send() 也是一样发邮件。
 *
 *   区别只在于：
 *   - 不用 RabbitMQ：用户等 3 秒
 *   - 用 RabbitMQ：用户等 1 毫秒
 *
 * ─── 还需要什么？ ───
 *
 *   要让项目跑起来，你需要先在本机安装并启动 RabbitMQ：
 *
 *   # 安装（macOS 用 Homebrew）
 *   brew install rabbitmq
 *
 *   # 启动
 *   brew services start rabbitmq
 *
 *   # 检查是否启动成功（看到管理页面说明 OK）
 *   浏览器打开 http://localhost:15672
 *   默认用户名/密码：guest / guest
 *
 *   # 对应的配置在 application.yaml 里：
 *   spring.rabbitmq.addresses = localhost   ← 连接本机
 *   spring.rabbitmq.username = admin        ← 登录名
 *   spring.rabbitmq.password = admin        ← 密码
 *
 *   注意：上面 yaml 里配的是 admin/admin，你需要去 RabbitMQ
 *   管理页面创建一个 admin 用户，或者把 yaml 改成 guest/guest。
 *
 * ══════════════════════════════════════════════════════════════════════════
 *  jsonMessageConverter 和 rabbitTemplate 是干什么的？
 * ══════════════════════════════════════════════════════════════════════════
 *
 * 之前报错：
 *   Attempt to deserialize unauthorized class java.util.CollSer
 *
 * 原因：
 *   我们用 rabbitTemplate.convertAndSend("mail", data) 发送一个 Map 时，
 *   Spring AMQP 默认使用"Java 序列化"把 Map 变成字节流。
 *   但新版 Spring AMQP 出于安全考虑，禁止反序列化不在白名单里的 Java 类。
 *   而 Map.of() 内部用了 java.util.CollSer，这个类不在白名单里，所以报错。
 *
 * 解决：
 *   换成 JSON 序列化，不用 Java 序列化。
 *
 *   Jackson2JsonMessageConverter 会把 Map 转成 JSON 字符串再发出去，
 *   接收端收到 JSON 后再转回 Map<String, Object>。
 *   JSON 不涉及 Java 类的反序列化，所以不会触发安全限制。
 *
 * 三个 @Bean 的分工：
 *   jsonMessageConverter()              — 提供"转换器"（JSON ↔ Java 对象）
 *   rabbitTemplate(...)                 — 提供"发消息的工具"，并告诉它用上面的转换器
 *   rabbitListenerContainerFactory(...) — 配置"收消息的容器"，也告诉它用上面的转换器
 *
 * ─── 为什么加了 rabbitTemplate 还要加 listenerContainerFactory？ ───
 *
 *   发送和接收是两条独立的管道：
 *     rabbitTemplate            → 负责"发消息出去"（生产者）
 *     @RabbitListener 的容器    → 负责"收进来的消息怎么解析"（消费者）
 *
 *   之前我只配了发送端用 JSON，但接收端还是默认的 Java 序列化。
 *   收到 JSON 消息后试图用 Java 反序列化去解析 → 又报错。
 *   现在两端都用 JSON 转换器，就一致了。
 *
 * ─── 如果重启后还是报错，需要清空队列里残留的旧消息 ───
 *
 *   之前已经发到队列里的消息是用 Java 序列化存的，它们还在队列里。
 *   新的 JSON 转换器不认识这些旧消息，一读到就报错。
 *
 *   解决办法：打开 http://localhost:15672 → 点 Queues → 点 mail →
 *   点 Purge（清空队列），把旧消息全部删掉。之后新发的消息就都是 JSON 格式了。
 */
