package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 项目启动与基础功能测试
 */
@SpringBootTest
class SpringBootFullStackApplicationTests {

    /**
     * 测试 Spring 上下文能否正常加载，并输出 BCrypt 加密示例
     */
    @Test
    void contextLoads() {
        System.out.println(new BCryptPasswordEncoder().encode("123456"));
    }

}
