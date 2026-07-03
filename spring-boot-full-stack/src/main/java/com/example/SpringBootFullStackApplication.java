package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动入口。
 * <p>使用 {@code @SpringBootApplication} 注解自动扫描并装配所有组件。</p>
 *
 * <h3>🎯 Spring Boot 启动流程（从 main() 到项目跑起来）</h3>
 *
 * <pre>
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │  ① main() 方法被 JVM 调用                                              │
 * │    SpringApplication.run(SpringBootFullStackApplication.class, args)   │
 * │    ↓                                                                   │
 * │  ② 创建 SpringApplication 实例                                         │
 * │    ├─ 判断项目类型（是 Servlet 还是 Reactive）                          │
 * │    ├─ 加载 META-INF/spring.factories 中的初始化器（Initializer）        │
 * │    └─ 加载 META-INF/spring.factories 中的监听器（Listener）             │
 * │    ↓                                                                   │
 * │  ③ 调用 run() 方法                                                     │
 * │    ├─ 启动计时器：记录启动耗时                                          │
 * │    ├─ 通知所有 SpringApplicationRunListener（准备开始）                 │
 * │    ├─ 准备 Environment（读取 application.yaml/yml 中的配置）            │
 * │    ├─ 打印 Banner（控制台那个 Spring 图案）                             │
 * │    ├─ 创建 ApplicationContext（IoC 容器）                               │
 * │    │    └─ Servlet 项目 → AnnotationConfigServletWebServerApplicationContext │
 * │    ├─ 调用 Initializer 初始化容器                                        │
 * │    └─ 通知监听器：容器已创建                                             │
 * │    ↓                                                                   │
 * │  ④ refreshContext() ⭐ 核心步骤（刷新 IoC 容器）                        │
 * │    ┌─────────────────────────────────────────────────────────────┐     │
 * │    │  AbstractApplicationContext.refresh()                       │     │
 * │    │  ┌─────────────────────────────────────────────────────┐   │     │
 * │    │  │ 1. prepareRefresh()       准备刷新，初始化属性源     │   │     │
 * │    │  │ 2. obtainFreshBeanFactory() 创建 BeanFactory          │   │     │
 * │    │  │ 3. prepareBeanFactory()   配置 BeanFactory 的标准特性 │   │     │
 * │    │  │ 4. postProcessBeanFactory() 空方法，留给子类扩展     │   │     │
 * │    │  │                                                       │   │     │
 * │    │  │ ⭐ 5. invokeBeanFactoryPostProcessors()               │   │     │
 * │    │  │     扫描 @SpringBootApplication 所在包及子包，        │   │     │
 * │    │  │     找到所有 @Component / @Service / @Controller 等   │   │     │
 * │    │  │     注册 BeanDefinition（就是把类信息登记到容器里）    │   │     │
 * │    │  │                                                       │   │     │
 * │    │  │ 6. registerBeanPostProcessors() 注册 Bean 后置处理器  │   │     │
 * │    │  │ 7. initMessageSource()       初始化国际化消息          │   │     │
 * │    │  │ 8. initApplicationEventMulticaster() 初始化事件广播器  │   │     │
 * │    │  │ 9. onRefresh()              空方法，留给子类扩展       │   │     │
 * │    │  │     内嵌 Web 服务器在这里启动（Tomcat 启动！）         │   │     │
 * │    │  │10. registerListeners()      注册监听器                │   │     │
 * │    │  │                                                       │   │     │
 * │    │  │ ⭐ 11. finishBeanFactoryInitialization()               │   │     │
 * │    │  │     实例化所有非懒加载的单例 Bean（每个 Bean 创建出来， │   │     │
 * │    │  │     处理依赖注入 @Autowired，调用 @PostConstruct 等）   │   │     │
 * │    │  │                                                       │   │     │
 * │    │  │12. finishRefresh()          刷新完成                    │   │     │
 * │    │  │    启动 Tomcat，监听端口，准备接收 HTTP 请求            │   │     │
 * │    │  └─────────────────────────────────────────────────────┘   │     │
 * │    └─────────────────────────────────────────────────────────────┘     │
 * │    ↓                                                                   │
 * │  ⑤ 容器刷新完成，Tomcat 已启动，项目正在监听端口                        │
 * │    ├─ 打印启动耗时：Started XXX in 3.456 seconds                        │
 * │    └─ 通知所有监听器：ApplicationReadyEvent（项目已就绪）                 │
 * │    ↓                                                                   │
 * │  ✅ 项目启动完成，开始接收 HTTP 请求                                     │
 * └────────────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h3>🔑 关键理解</h3>
 * <ul>
 *   <li><b>main() + @SpringBootApplication</b> = 启动的起点 + 配置的声明</li>
 *   <li><b>SpringApplication.run()</b> 做了两件大事：
 *      创建 IoC 容器 + 刷新容器（加载所有 Bean）</li>
 *   <li><b>@SpringBootApplication</b> 其实是一个组合注解：
 *      {@code @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan}</li>
 *   <li><b>Tomcat 什么时候启动？</b>在 refresh() 的最后一步 finishRefresh() 中启动</li>
 *   <li><b>Bean 什么时候创建？</b>在第 11 步 finishBeanFactoryInitialization() 中创建</li>
 *   <li><b>配置文件什么时候读？</b>在第 ③ 步准备 Environment 时读取 application.yaml</li>
 * </ul>
 */
@SpringBootApplication
public class SpringBootFullStackApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootFullStackApplication.class, args);
    }

}


/*
 * ══════════════════════════════════════════════════════════════════════════════
 *  Java 后端技术栈 — 完整架构认知框架
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * ─── 一、分层架构（自上而下） ───
 *
 *   一个典型的后端项目从代码组织上分为四层，每一层只管自己的事：
 *
 *   ┌────────────────────────────────────────────────────────────────────┐
 *   │  Controller 层（表现层）                                            │
 *   │  ─────────────────────                                              │
 *   │  职责：接收 HTTP 请求、解析参数、返回 JSON 响应                      │
 *   │  典型代码：@RestController, @GetMapping, @PostMapping               │
 *   │  依赖关系：Controller → Service（调用 Service 的方法）              │
 *   │  例子：AuthorizeController.askVerifyCode()                          │
 *   │        从 URL 里取出 email 和 type，调 Service，返回结果            │
 *   ├────────────────────────────────────────────────────────────────────┤
 *   │  Service 层（业务逻辑层）                                           │
 *   │  ─────────────────────                                              │
 *   │  职责：实现业务规则、组合多个操作完成一个功能                        │
 *   │  典型代码：@Service, @Transactional                                 │
 *   │  依赖关系：Service → Mapper（查数据库） + 其他 Service + 工具类     │
 *   │  例子：AccountService.registerEmailVerifyCode()                     │
 *   │        检查限流 → 生成验证码 → 发 MQ 消息 → 存 Redis               │
 *   ├────────────────────────────────────────────────────────────────────┤
 *   │  Mapper/Repository 层（数据访问层）                                 │
 *   │  ────────────────────────                                           │
 *   │  职责：和数据库打交道，CRUD                                         │
 *   │  典型代码：@Mapper extends BaseMapper<Entity>                       │
 *   │  例子：AccountMapper.selectOne() → 生成 SQL → MySQL 执行 → 返回结果 │
 *   ├────────────────────────────────────────────────────────────────────┤
 *   │  Entity/DTO/VO 层（数据模型层）                                     │
 *   │  ────────────────────────                                           │
 *   │  职责：定义数据结构，不包含业务逻辑                                  │
 *   │  Entity：对应数据库表（Account → account 表）                    │
 *   │  DTO：数据传输对象（Service 内部传递用）                             │
 *   │  VO：视图对象（返回给前端的格式）                                   │
 *   └────────────────────────────────────────────────────────────────────┘
 *
 *   关键规则：上层依赖下层，不能反向依赖。
 *   Controller 不能直接调 Mapper，必须经过 Service。
 *
 * ─── 二、一个请求的完整生命周期 ───
 *
 *   用户点击"获取验证码"→ 请求到达服务器，经过的全部环节：
 *
 *   ┌─ 用户浏览器 ───────────────────────────────────────────────────────┐
 *   │  GET http://localhost:8080/api/auth/ask-code?email=xxx&type=register│
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ① Tomcat（Web 服务器）───────────────────────────────────────────┐
 *   │  从操作系统接收到 TCP 连接 → 解析 HTTP 协议 → 包装成              │
 *   │  HttpServletRequest 对象 → 从线程池取一个线程处理                  │
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ② Filter 链 ─────────────────────────────────────────────────────┐
 *   │  按 @Order 顺序依次经过：                                          │
 *   │  ├─ CorsFilter（-102） → 加跨域头                                  │
 *   │  ├─ Spring Security 过滤器链（-100）                               │
 *   │  │   ├─ JwtAuthorizeFilter → 检查 JWT Token                       │
 *   │  │   └─ 安全检查 → /api/auth/** 允许放行                          │
 *   │  └─ ... 其他过滤器                                                 │
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ③ DispatcherServlet ─────────────────────────────────────────────┐
 *   │  Spring MVC 的核心入口。根据 URL 找到对应的 Controller 方法：      │
 *   │  匹配 /api/auth/ask-code → AuthorizeController.askVerifyCode()     │
 *   │  把 @RequestParam 的值绑定到方法参数（email, type）               │
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ④ Controller ────────────────────────────────────────────────────┐
 *   │  @Validated 校验参数：email 格式、type 正则                        │
 *   │  调 Service → 拿到结果 → 封装 RestBean → 返回                     │
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ⑤ Service ───────────────────────────────────────────────────────┐
 *   │  registerEmailVerifyCode()                                        │
 *   │  ├─ ① synchronized(address.intern()) 锁住同一 IP                 │
 *   │  ├─ ② verifyLimit() → 检查 Redis 限流标记                        │
 *   │  ├─ ③ 生成 6 位随机码                                             │
 *   │  ├─ ④ rabbitTemplate.convertAndSend() → 发到 RabbitMQ             │
 *   │  │     这一步通过网络把数据发给 RabbitMQ 服务器（1ms）            │
 *   │  └─ ⑤ stringRedisTemplate.opsForValue().set() → 存验证码到 Redis  │
 *   └─────────────────────────┬──────────────────────────────────────────┘
 *                             │
 *                             ▼
 *   ┌─ ⑥ 响应返回 ──────────────────────────────────────────────────────┐
 *   │  RestBean.success() → FastJSON2 序列化成 JSON 字符串 →            │
 *   │  写入 HTTP 响应体 → Tomcat 把响应发回浏览器                       │
 *   └────────────────────────────────────────────────────────────────────┘
 *                             │
 *                             此时用户已收到"已发送"的响应 ✅
 *                             但邮件还没发出去，后台还在处理：
 *                             │
 *                             ▼
 *   ┌─ ⑦ MQ 后台线程（独立于 HTTP 线程）───────────────────────────────┐
 *   │  MailQueueListener 的后台线程一直阻塞在 RabbitMQ 连接上，          │
 *   │  感知到队列里有新消息 → 取出来 → 调 sendMailMessage() →           │
 *   │  JavaMailSender.send() → 连接 Mailpit（localhost:1025）→ 邮件写入  │
 *   └────────────────────────────────────────────────────────────────────┘
 *
 * ─── 三、线程模型 ───
 *
 *                     ┌──────────────────┐
 *                     │  用户请求 A      │
 *                     └────────┬─────────┘
 *                              │
 *                              ▼
 *   ┌─────────────────────────────────────────────┐
 *   │  Tomcat 线程池                               │
 *   │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │
 *   │  │线程1  │ │线程2  │ │线程3  │ │线程4  │      │
 *   │  │请求A  │ │请求B  │ │空闲   │ │空闲   │      │
 *   │  └──┬───┘ └──┬───┘ └──────┘ └──────┘      │
 *   └─────┼────────┼──────────────────────────────┘
 *         │        │
 *         ▼        ▼
 *   ┌──────────────────────────────┐
 *   │  单例 Bean（整个进程只有一个实例） │
 *   │                              │
 *   │  Controller / Service /      │
 *   │  Mapper / RedisTemplate /    │
 *   │  RabbitTemplate              │
 *   │                              │
 *   │  多个线程同时调同一个对象的   │
 *   │  同一个方法                  │
 *   └──────────────────────────────┘
 *         │
 *         │  ┌──────────────────────────────────┐
 *         │  │  MQ 后台线程池                    │
 *         │  │  ┌────────┐ ┌────────┐          │
 *         └──│──│ 线程5   │ │ 线程6   │          │
 *            │  │ 等消息  │ │ 等消息  │          │
 *            │  └────────┘ └────────┘          │
 *            └──────────────────────────────────┘
 *
 *   共享规则：
 *     ┌──────────────┬──────────────────┬──────────────────────┐
 *     │              │ 存储位置          │ 范围                  │
 *     ├──────────────┼──────────────────┼──────────────────────┤
 *     │ Bean 实例    │ 堆内存            │ 全局共享（所有线程）   │
 *     │ 方法参数     │ 线程栈            │ 仅当前线程            │
 *     │ 局部变量     │ 线程栈            │ 仅当前线程            │
 *     │ ThreadLocal  │ 堆 + 线程绑定     │ 仅当前线程            │
 *     │ 请求对象     │ 堆（但每个请求    │ 仅当前线程            │
 *     │              │ 有独立实例）      │                      │
 *     └──────────────┴──────────────────┴──────────────────────┘
 *
 * ─── 四、六边形架构：项目与外部系统的交互 ───
 *
 *   你的项目不是孤岛，它连接了多个外部系统：
 *
 *                  ┌──────────────┐
 *                  │   浏览器      │
 *                  │  (前端用户)   │
 *                  └──────┬───────┘
 *                         │ HTTP（:8080）
 *                         ▼
 *   ┌────────────────────────────────────────────────────┐
 *   │              你的 Spring Boot 项目                  │
 *   │                                                    │
 *   │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
 *   │  │ Controller│→│ Service  │→│ Mapper           │ │
 *   │  │ (接收请求)│  │ (业务逻辑)│  │ (数据库操作)     │ │
 *   │  └──────────┘  └────┬─────┘  └────────┬─────────┘ │
 *   │                     │                  │           │
 *   │               ┌─────┴─────┐     ┌──────┴──────┐   │
 *   │               │RabbitTemp │     │RedisTemplate│   │
 *   │               │(发MQ消息)  │     │(存验证码)    │   │
 *   │               └─────┬─────┘     └──────┬──────┘   │
 *   │                     │                  │           │
 *   │               ┌─────┴─────┐            │           │
 *   │               │MailQueue  │            │           │
 *   │               │Listener   │            │           │
 *   │               │(收MQ消息)  │            │           │
 *   │               └─────┬─────┘            │           │
 *   │                     │                  │           │
 *   └─────────────────────┼──────────────────┼───────────┘
 *                         │                  │
 *                         ▼                  ▼
 *                ┌──────────────┐    ┌──────────────┐
 *                │  RabbitMQ    │    │    Redis     │
 *                │  localhost:  │    │  localhost:  │
 *                │  5672        │    │  6379        │
 *                └──────────────┘    └──────────────┘
 *
 *                         ▼
 *                ┌──────────────┐    ┌──────────────┐
 *                │   Mailpit    │    │    MySQL     │
 *                │  localhost:  │    │  localhost:  │
 *                │  1025        │    │  3306        │
 *                └──────────────┘    └──────────────┘
 *
 *   每一种外部系统都有对应的客户端工具，它们都是线程安全的：
 *   ┌─────────┬──────────────┬──────────┬──────────────┐
 *   │ 外部系统 │ 客户端        │ 端口     │ 协议          │
 *   ├─────────┼──────────────┼──────────┼──────────────┤
 *   │ MySQL   │ Mapper        │ 3306     │ JDBC         │
 *   │ Redis   │ RedisTemplate │ 6379     │ RESP         │
 *   │ RabbitMQ│ RabbitTemplate│ 5672     │ AMQP         │
 *   │ SMTP    │ JavaMailSender│ 1025     │ SMTP         │
 *   └─────────┴──────────────┴──────────┴──────────────┘
 *
 * ─── 五、依赖注入的本质 ───
 *
 *   @Resource AccountService accountService;
 *   @Resource StringRedisTemplate stringRedisTemplate;
 *   @Resource AmqpTemplate rabbitTemplate;
 *
 *   这些字段的值是谁赋的？不是你自己 new 的。流程是：
 *
 *   ① 项目启动时，Spring 扫描所有 @Component / @Service / @Controller
 *   ② 发现 AccountService → 创建它的实例（new AccountService()）
 *   ③ 发现 AccountService 里有 @Resource AmqpTemplate rabbitTemplate
 *   ④ 去容器里找 AmqpTemplate 的实现 → 找到 RabbitTemplate 的实例
 *   ⑤ 把 RabbitTemplate 实例赋值给 rabbitTemplate 字段（反射）
 *   ⑥ 赋值完成后，AccountService 才算"创建完成"，可以被其他人使用了
 *
 *   这就是 IoC（控制反转）——不是你控制对象的创建，而是容器帮你创建和组装。
 *
 * ─── 六、配置的中心化 ───
 *
 *   application.yaml 是项目的"总控制台"。所有外部系统的连接信息都
 *   写在这里，Spring Boot 自动读取并配置对应的客户端：
 *
 *   application.yaml 配置项              → 自动配置的客户端
 *   ─────────────────────────           ─────────────────
 *   spring.datasource.*                 → DataSource（连 MySQL）
 *   spring.data.redis.*                 → RedisTemplate（连 Redis）
 *   spring.rabbitmq.*                   → RabbitTemplate（连 RabbitMQ）
 *   spring.mail.*                       → JavaMailSender（发邮件）
 *   spring.security.jwt.*               → 自定义读取（JwtUtils）
 *
 *   然后通过 @Value("${...}") 或 @ConfigurationProperties 读取。
 *
 * ─── 七、总结 ───
 *
 *   后端技术栈的核心就这几件事：
 *
 *   ① 接收请求      → Controller 层（@RestController）
 *   ② 处理业务      → Service 层（@Service）
 *   ③ 存取数据      → Mapper 层（MyBatis-Plus）
 *   ④ 连接外部系统  → Redis / RabbitMQ / SMTP
 *   ⑤ 返回响应      → JSON（RestBean + FastJSON2）
 *   ⑥ 安全控制      → Spring Security + JWT
 *   ⑦ 所有类的组装  → IoC 容器（依赖注入）
 *   ⑧ 所有配置      → application.yaml
 *
 *   一个请求从进来到出去，走的就是 ①→⑥→②→③→④→⑤ 这条线。
 *   理解了这条线，你就理解了整个后端。
 */
