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
