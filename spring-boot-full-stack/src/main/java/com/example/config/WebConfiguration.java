package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Web 通用配置 — 存放不和 Security 产生循环依赖的公共 Bean。
 *
 * ════════════════════════════════════════════════════════════════════
 *  为什么 PasswordEncoder 要放在这里，而不是 SecurityConfiguration 里？
 * ════════════════════════════════════════════════════════════════════
 *
 * 先搞懂一个概念：Spring 是怎么"组装"对象的？
 *
 *   假设你写了：
 *     @Service public class AService {
 *         @Resource BService bService;
 *     }
 *     @Service public class BService {
 *         @Resource AService aService;
 *     }
 *
 *   Spring 启动时要做的事：
 *     要创建 AService → 发现它需要 BService → 去创建 BService
 *                                              ↓
 *                                  发现 BService 需要 AService → 去创建 AService
 *                                                                   ↓
 *                                                       又发现需要 BService → 无限循环 ❌
 *
 *   这就是循环依赖：A 依赖 B，B 又依赖 A，两个都没法先创建。
 *
 * ─── 回到你的项目 ───
 *
 *   SecurityConfiguration 注入了一个 UserDetailsService（即 AccountService）
 *   AccountService 又注入了 PasswordEncoder
 *   PasswordEncoder 定义在 SecurityConfiguration 里
 *
 *   整个链条：
 *
 *   ┌──────────────────────────┐
 *   │  SecurityConfiguration   │
 *   │                          │
 *   │  ① @Resource            │
 *   │     UserDetailsService ──┼────┐
 *   │                          │    │
 *   │  ③ @Bean                │    │  需要先有
 *   │     passwordEncoder() ◄──┼──┐ │
 *   └──────────────────────────┘  │ │
 *                                 │ │
 *                                 ▼ │
 *                       ┌──────────────────────┐
 *                       │   AccountService     │
 *                       │                      │
 *                       │  ② @Resource        │
 *                       │     PasswordEncoder ─┼─┘
 *                       └──────────────────────┘
 *
 *   第 1 步：SecurityConfiguration 要创建，发现需要 AccountService
 *   第 2 步：AccountService 要创建，发现需要 PasswordEncoder
 *   第 3 步：PasswordEncoder 定义在 SecurityConfiguration 里，
 *           所以需要 SecurityConfiguration 先创建 → 回到第 1 步 → 死循环 💀
 *
 *   这就是报错里写的：
 *     ┌─────┐
 *     |  securityConfiguration
 *     ↑     ↓
 *     |  accountService
 *     └─────┘
 *
 * ─── 为什么把 PasswordEncoder 挪出来就好了？ ───
 *
 *   现在 Spring 的创建顺序变成了：
 *
 *   ① WebConfiguration 创建（独立，不依赖任何人）
 *         │
 *         ▼
 *     产生 @Bean PasswordEncoder  ← 现在它是独立存在的了
 *         │
 *         ▼
 *   ② AccountService 创建（注入 PasswordEncoder，已经存在了）
 *         │
 *         ▼
 *   ③ SecurityConfiguration 创建（注入 AccountService，已经存在了）
 *
 *   每一步需要的对象都已经有了，不再有循环。
 *
 *   ─── 一句话总结 ───
 *
 *   循环依赖就是"鸡生蛋蛋生鸡"的问题。
 *   A 要创建需要 B，B 要创建需要 A，两个都卡住。
 *   把公共的部分（PasswordEncoder）放到中间人（WebConfiguration）手里，
 *   先创建中间人，再依次创建 A 和 B，链条就通了。
 */
@Configuration
public class WebConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
