package com.example;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RabbitMQ 功能测试
 *
 * 测试方案：
 *   生产环境中的 MailQueueListener 一直在监听 "mail.queue" 队列，
 *   如果往 "mail.queue" 发消息，会被它消费掉（然后尝试发邮件）。
 *
 *   为了单独测试 MQ 的收发是否正常，这里使用一个临时测试队列
 *   "test.mq.queue"（自动删除），不和生产监听器冲突。
 *
 *   测试步骤：
 *   ① 在 RabbitMQ 上创建一个临时测试队列
 *   ② 往这个队列发一条 JSON 字符串消息
 *   ③ 从这个队列接收消息
 *   ④ 断言收到的 JSON 内容是否正确
 */
@SpringBootTest
public class RabbitMQTests {

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    AmqpAdmin amqpAdmin;

    @Test
    void testSendAndReceiveJsonMessage() {
        // ===== ① 创建测试队列 =====
        // 参数：name, durable, exclusive, autoDelete
        // exclusive=true → 仅当前连接可见，断开后自动删除，不留垃圾
        Queue testQueue = new Queue("test.mq.queue", false, true, false);
        amqpAdmin.declareQueue(testQueue);

        // ===== ② 发送 JSON 消息 =====
        JSONObject data = new JSONObject();
        data.put("type", "register");
        data.put("email", "test@example.com");
        data.put("code", 123456);
        String json = data.toJSONString();

        rabbitTemplate.convertAndSend("test.mq.queue", json);
        System.out.println("✅ 已发送消息: " + json);

        // ===== ③ 接收消息（最多等 3 秒） =====
        String received = (String) rabbitTemplate.receiveAndConvert("test.mq.queue", 3000);
        System.out.println("📩 收到消息: " + received);

        // ===== ④ 验证 =====
        assertNotNull(received, "应该能收到消息，但 received=null。说明 RabbitMQ 连接或收发异常");

        JSONObject obj = JSONObject.parseObject(received);
        assertEquals("register", obj.getString("type"), "type 字段不正确");
        assertEquals("test@example.com", obj.getString("email"), "email 字段不正确");
        assertEquals(123456, obj.getIntValue("code"), "code 字段不正确");

        System.out.println("🎉 RabbitMQ 收发 JSON 字符串消息测试通过！");
    }
}
