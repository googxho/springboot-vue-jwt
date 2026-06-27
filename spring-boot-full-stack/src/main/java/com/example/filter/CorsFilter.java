package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ================================================================
 *  跨域配置过滤器（CorsFilter）
 * ================================================================
 *
 *  ▐ 背景：什么是跨域？
 *  ─────────────────────────────────────────────────────────────────
 *  浏览器出于安全考虑，实施"同源策略"（Same-Origin Policy）。
 *  只有当请求的 协议、域名、端口 三者完全一致时，浏览器才允许
 *  JavaScript 读取响应内容。否则就构成"跨域请求"。
 *
 *  前后端分离项目中，前端（如 http://localhost:5173）和后端
 *  （如 http://localhost:8080）通常端口不同，所以前端发起的
 *  AJAX/Fetch 请求都是跨域请求，需要后端配合处理。
 *
 *  ▐ CORS 工作原理（浏览器视角）
 *  ─────────────────────────────────────────────────────────────────
 *  跨域请求分两种场景：
 *
 *  ┌─ 简单请求（GET/POST/HEAD，且 Content-Type 有限制）
 *  │  浏览器直接发送实际请求，并检查响应头中是否有
 *  │  Access-Control-Allow-Origin，有则放行，无则报错。
 *  │
 *  └─ 预检请求（非简单请求，如 PUT/DELETE/自定义请求头）
 *      浏览器先发一个 OPTIONS 请求（预检），询问服务器：
 *      "允许跨域吗？允许哪些方法？允许哪些请求头？"
 *      服务器回复后，浏览器根据响应头判断是否可以发送实际请求。
 *
 *  ┌──────────────────────────────────────────────────────────────┐
 *  │  前端 Fetch                     后端服务器                    │
 *  │  ──────────                      ──────────                  │
 *  │      │                              │                        │
 *  │      │── OPTIONS /api/xxx ──────►   │  ← 预检请求            │
 *  │      │                              │                        │
 *  │      │◄── CORS 响应头 ─────────────│                        │
 *  │      │   (Allow-Origin, Methods等)  │                        │
 *  │      │                              │                        │
 *  │      │── GET /api/xxx ──────────►   │  ← 实际请求            │
 *  │      │   Authorization: Bearer xxx  │                        │
 *  │      │                              │                        │
 *  │      │◄── 响应数据 ────────────────│                        │
 *  └──────────────────────────────────────────────────────────────┘
 *
 *  ▐ 本过滤器在系统架构中的位置
 *  ─────────────────────────────────────────────────────────────────
 *  整个请求处理链的层级（从外到内）：
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  ① Tomcat / Undertow 容器（接收 HTTP 请求）                  │
 *  │                                                             │
 *  │  ② CorsFilter（本过滤器）                                    │
 *  │      @Order(Const.ORDER_CORS) = -102                        │
 *  │      ├─ 给所有响应添加跨域头                                  │
 *  │      ├─ OPTIONS 预检请求 → 直接返回 200，不往下走             │
 *  │      └─ 其他请求 → 继续向下                                  │
 *  │                                                             │
 *  │  ③ Spring Security 过滤器链                                  │
 *  │      SecurityProperties.DEFAULT_FILTER_ORDER = -100         │
 *  │      ├─ JwtAuthorizeFilter → 解析 JWT 并设置认证信息         │
 *  │      ├─ 其他 Security 内置过滤器                             │
 *  │      └─ 最终判断：请求是否需要认证？                          │
 *  │                                                             │
 *  │  ④ DispatcherServlet → Controller                          │
 *  │      处理业务逻辑，返回 JSON                                 │
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  ⚠️ 关键设计：为什么 CorsFilter 必须在 Spring Security 之前？
 *     ─────────────────────────────────────────────────────
 *     如果 Spring Security 先于 CorsFilter 执行，OPTIONS 预检
 *     请求会因为没有携带 JWT 而被 Security 拦截（返回 401/403），
 *     浏览器收不到 CORS 响应头，就会报跨域错误，实际请求永远不会
 *     发出去。
 *
 *     通过 @Order(Const.ORDER_CORS) = -102，比 Security 的
 *     默认顺序 -100 更小（优先级更高），确保 CorsFilter 先执行，
 *     OPTIONS 请求在进入 Security 之前就被拦截并返回 200，
 *     浏览器收到 CORS 头后才会继续发送实际请求。
 *
 *  ▐ 配置来源
 *  ─────────────────────────────────────────────────────────────────
 *  三个配置项（origin / credentials / methods）均从
 *  application.yaml 中读取，通过 @Value 注入：
 *
 *    spring:
 *      web:
 *        cors:
 *          origin: '*'          # 允许的来源，* 表示任意来源
 *          credentials: false   # 是否允许携带 Cookie
 *          methods: '*'         # 允许的 HTTP 方法
 *
 *  生产环境中应将 origin 改为具体的前端域名，以提高安全性。
 * ================================================================
 */
@Component                      // 注册为 Spring Bean，启动时自动被扫描并注册到容器
@Order(Const.ORDER_CORS)        // 指定过滤器优先级，-102 确保在 Spring Security 之前执行
public class CorsFilter extends HttpFilter {

    /**
     * 允许跨域的来源站点。
     * 从配置 spring.web.cors.origin 读取。
     * '*' 表示允许任意来源（开发时常用），生产环境建议改为具体域名。
     */
    @Value("${spring.web.cors.origin}")
    String origin;

    /**
     * 是否允许跨域请求携带凭证（Cookie / Authorization 头等）。
     * 从配置 spring.web.cors.credentials 读取。
     * true  = 允许携带凭证（此时 origin 不能为 '*'，必须指定具体域名）
     * false = 不允许携带凭证
     */
    @Value("${spring.web.cors.credentials}")
    boolean credentials;

    /**
     * 允许的 HTTP 方法列表。
     * 从配置 spring.web.cors.methods 读取。
     * '*' 表示允许所有标准方法。
     */
    @Value("${spring.web.cors.methods}")
    String methods;

    /**
     * ============================================================
     *  核心过滤方法 —— 每个 HTTP 请求都会经过这里
     * ============================================================
     *
     * 执行流程：
     *
     *  ┌─ 请求到达 ─────────────────────────────────────────────┐
     *  │                                                        │
     *  │  步骤 1：添加跨域响应头                                 │
     *  │    ├─ Access-Control-Allow-Origin  ← 允许的来源         │
     *  │    ├─ Access-Control-Allow-Methods ← 允许的 HTTP 方法   │
     *  │    ├─ Access-Control-Allow-Headers ← 允许的自定义请求头 │
     *  │    └─ Access-Control-Allow-Credentials ← 是否允许凭证   │
     *  │                                                        │
     *  │  步骤 2：判断是否是 OPTIONS 预检请求                    │
     *  │    ├─ 是 → 直接返回 200（不需要再往下处理）             │
     *  │    └─ 否 → 放行到下一个过滤器（Spring Security）        │
     *  │                                                        │
     *  └────────────────────────────────────────────────────────┘
     *
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param chain    过滤器链，调用 doFilter 将请求交给下一个过滤器
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // ===== 第 1 步：为当前请求添加跨域响应头 =====
        // 无论是什么请求（OPTIONS / GET / POST / PUT / DELETE 等），
        // 都先加上 CORS 响应头，这样浏览器才能正确解析。
        this.addCorsHeader(request, response);

        // ===== 第 2 步：处理 OPTIONS 预检请求 =====
        // 对于跨域"非简单请求"，浏览器会先发一个 OPTIONS 请求来"
        // 试探"服务器是否允许跨域。这个请求不带业务数据，也不带
        // JWT 令牌，所以它不应该进入 Spring Security 的认证流程。
        //
        // 如果让 OPTIONS 请求继续往下走，Spring Security 会发现
        // 没有认证信息，直接返回 401/403，浏览器收不到 CORS 头，
        // 就会判定跨域被拒绝，实际请求永远不会发出。
        //
        // 因此这里直接返回 200，浏览器收到 CORS 响应头后，
        // 就会继续发送实际的业务请求。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ===== 第 3 步：放行非 OPTIONS 请求 =====
        // 实际业务请求（GET / POST / PUT / DELETE 等）继续往下走，
        // 进入 Spring Security 过滤器链进行 JWT 认证和授权检查。
        chain.doFilter(request, response);
    }

    /**
     * ============================================================
     *  添加跨域响应头
     * ============================================================
     *
     *  向 HTTP 响应中添加四个 CORS 相关的响应头，告诉浏览器：
     *
     *  ┌────────────────────────────────────────────────────────┐
     *  │  响应头                             作用               │
     *  ├────────────────────────────────────────────────────────┤
     *  │  Access-Control-Allow-Origin      允许跨域的来源       │
     *  │  Access-Control-Allow-Methods     允许的 HTTP 方法     │
     *  │  Access-Control-Allow-Headers     允许的自定义请求头   │
     *  │  Access-Control-Allow-Credentials 是否允许携带凭证     │
     *  └────────────────────────────────────────────────────────┘
     *
     *  浏览器收到这些响应头后，会检查实际请求是否在允许范围内：
     *  - 来源是否在 Allow-Origin 列表中
     *  - 方法是否在 Allow-Methods 列表中
     *  - 请求头是否在 Allow-Headers 列表中
     *  如果都符合，浏览器才会把响应交给前端的 JavaScript 代码；
     *  如果不符合，浏览器会拦截响应并抛出一个跨域错误。
     *
     * @param request  HTTP 请求（用于动态获取请求来源）
     * @param response HTTP 响应（用于写入 CORS 响应头）
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        // 允许的原始站点（前端地址）
        response.addHeader("Access-Control-Allow-Origin", this.resolveOrigin(request));
        // 允许的 HTTP 请求方法
        response.addHeader("Access-Control-Allow-Methods", this.resolveMethod());
        // 允许的自定义请求头——前端在请求中带了哪些额外头，这里就要允许哪些
        // Authorization: 用于传递 JWT 令牌
        // Content-Type:   用于指定请求体格式（application/json 等）
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        // 如果配置允许携带凭证，则添加该响应头
        // 注意：当 Allow-Credentials = true 时，Allow-Origin 不能为 '*'，
        //       必须指定具体域名，否则浏览器会拒绝。
        if(credentials) {
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    /**
     * 解析配置文件中允许的 HTTP 请求方法。
     *
     * 如果配置为通配符 '*'，则展开为所有标准 HTTP 方法列表；
     * 否则直接返回配置的具体方法字符串（如 "GET, POST"）。
     *
     * @return 以逗号分隔的 HTTP 方法列表字符串
     */
    private String resolveMethod(){
        return methods.equals("*") ? "GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH" : methods;
    }

    /**
     * 解析配置文件中允许的跨域来源。
     *
     * 如果配置为通配符 '*'，则动态获取当前请求的 Origin 请求头值，
     * 并将其设为允许的来源（即"允许来自任意站点的跨域请求"）；
     * 否则直接返回配置的具体域名。
     *
     * 为什么 '*' 时要取请求的 Origin？
     * 因为 Access-Control-Allow-Origin 不能直接设为 '*'，
     * 同时又想允许所有来源。取 Origin 请求头的值相当于动态回显，
     * 效果等同于允许所有来源，但比 '*' 更灵活（未来可扩展）。
     *
     * @param request HTTP 请求（从中获取 Origin 请求头）
     * @return 允许跨域的来源字符串
     */
    private String resolveOrigin(HttpServletRequest request){
        return origin.equals("*") ? request.getHeader("Origin") : origin;
    }
}



// ============================================================================
//  附录：Servlet 容器机制 & DispatcherServlet 全景概览
// ============================================================================
//
//  ▐ 一、什么是 Servlet 容器？
//  ────────────────────────────────────────────────────────────────────────────
//  Servlet 容器（如 Tomcat / Undertow / Jetty）本质上是一个"Java 写的 HTTP
//  服务器"。它的职责是：
//
//    ① 监听端口（默认 8080），接收 TCP 连接
//    ② 解析 HTTP 请求报文（纯文本）→ 组装成 HttpServletRequest 对象
//    ③ 按规则找到对应的 Servlet
//    ④ 按顺序执行过滤器链（Filter Chain）
//    ⑤ 调用 Servlet 处理业务逻辑
//    ⑥ 把 HttpServletResponse 序列化成 HTTP 响应报文 → 发回浏览器
//
//
//  ▐ 二、Servlet 规范中的三大核心接口
//  ────────────────────────────────────────────────────────────────────────────
//
//  ┌──────────────────────────────────────────────────────────────────────┐
//  │  Servlet             Filter               FilterChain               │
//  │  （处理器）          （拦截器）            （链）                    │
//  │                                                                      │
//  │  init()              init()               内部维护一个索引指针：     │
//  │  service(req, res)   doFilter(req,        按注册顺序排列所有 Filter  │
//  │  destroy()             res, chain)        每次 chain.doFilter()     │
//  │                       destroy()           将索引 +1，调用下一个      │
//  │                                                                      │
//  │  真正处理请求        在 Servlet 执行前/后  最后一个 Filter 调用完    │
//  │                     做拦截或增强           → 到达 Servlet           │
//  └──────────────────────────────────────────────────────────────────────┘
//
//  Filter 的三种行为：
//    • 拦截：不调用 chain.doFilter()，直接返回响应
//      → 本类对 OPTIONS 预检请求的处理就是拦截
//    • 修改：在 chain.doFilter() 前后修改 request / response
//      → 本类的 addCorsHeader() 就是在调用前加了响应头
//    • 放行：调用 chain.doFilter() 交给下一个
//
//
//  ▐ 三、Spring Boot 如何把这一切串起来？
//  ────────────────────────────────────────────────────────────────────────────
//
//  Spring Boot 启动时做了四件事：
//
//  ① 内嵌 Servlet 容器
//     根据 classpath 自动选择 Tomcat 或 Undertow，以嵌入方式启动
//     （相当于在 Java 进程内部启动了 Tomcat）
//
//  ② 扫描 @Component Filter 并注册到容器
//     发现 CorsFilter 实现了 Filter 接口 + @Component 注解
//     → 自动创建 FilterRegistrationBean
//     → 调用 servletContext.addFilter("corsFilter", corsFilter)
//     → 按 @Order 值排序，插入到 Servlet 容器的过滤器链中
//
//  ③ 创建 DispatcherServlet 并注册
//     这是 Spring MVC 的核心，也是一个 Servlet
//     → 自动注册到 Servlet 容器，映射路径 "/"（拦截所有请求）
//
//  ④ Spring Security 把自己包装成一个 Filter
//     → DelegatingFilterProxy 被注册到容器，@Order(-100)
//     → 它的 doFilter() 内部调用 SecurityConfiguration 定义的过滤器链
//
//
//  ▐ 四、DispatcherServlet 详解
//  ────────────────────────────────────────────────────────────────────────────
//
//  DispatcherServlet 是 Spring MVC 的"前端控制器"（Front Controller）。
//  它本身就是一个 Servlet（继承 HttpServlet），但其职责不是处理具体业务，
//  而是"分发"——找到正确的 Controller 方法来处理当前请求。
//
//  工作流程（6 步）：
//
//  ┌────────────────────────────────────────────────────────────────────┐
//  │  请求到达 DispatcherServlet                                        │
//  │                                                                    │
//  │  ① HandlerMapping                                                  │
//  │    问："/api/auth/login" 这个 URL 谁来处理？                        │
//  │    答：AuthorizeController.login()                                 │
//  │                                                                    │
//  │  ② HandlerAdapter                                                  │
//  │    问：这个方法的参数是什么？                                       │
//  │    答：@RequestBody Account account                                │
//  │    做：把 JSON 反序列化成 Account 对象                             │
//  │                                                                    │
//  │  ③ 调用目标方法                                                    │
//  │    AuthorizeController.login(account) → RestBean.success(token)    │
//  │                                                                    │
//  │  ④ HandlerMethodReturnValueHandler                                 │
//  │    问：返回值怎么处理？                                             │
//  │    答：@ResponseBody → 序列化成 JSON                               │
//  │                                                                    │
//  │  ⑤ MessageConverter                                                │
//  │    做：把 Java 对象 → JSON 字符串                                  │
//  │                                                                    │
//  │  ⑥ 写入 HttpServletResponse                                        │
//  │    response.getWriter().write(jsonString)                          │
//  └────────────────────────────────────────────────────────────────────┘
//
//  没有 DispatcherServlet 的话，你需要为每个 URL 手动写 Servlet：
//    /api/auth/login   → LoginServlet
//    /api/auth/register → RegisterServlet
//    /api/user/xxx      → UserServlet
//  每个 Servlet 里还要手动解析参数、手动序列化 JSON...
//  DispatcherServlet 替你省去了所有这些重复劳动。
//
//
//  ▐ 五、一次完整请求的全景链路
//  ────────────────────────────────────────────────────────────────────────────
//
//  假设前端发送：POST http://localhost:8080/api/auth/login
//  请求体：{"username":"admin","password":"123"}
//  请求头：Content-Type: application/json
//
//  ┌───────────────────────────────────────────────────────────────────────┐
//  │  层级 1：操作系统网络层                                               │
//  │    DNS 解析 localhost → TCP 三次握手 :8080 → 发送 HTTP 报文          │
//  │                                                                       │
//  │  层级 2：Tomcat Connector                                             │
//  │    解析 HTTP 报文 → 组装 HttpServletRequest / HttpServletResponse     │
//  │                                                                       │
//  │  层级 3：Servlet 容器过滤器链（FilterChain）                          │
//  │    ┌──────────────────────────────────────────────────────────────┐   │
//  │    │ index=0: CorsFilter               @Order(-102)               │   │
//  │    │   ├─ addCorsHeader() → 加 CORS 响应头                         │   │
//  │    │   ├─ 不是 OPTIONS → chain.doFilter() 放行                     │   │
//  │    │   └─ 是 OPTIONS → 直接返回 200（短路）                        │   │
//  │    │                                                              │   │
//  │    │ index=1: DelegatingFilterProxy       @Order(-100)             │   │
//  │    │   ├─ JwtAuthorizeFilter：检查 JWT，无则跳过                    │   │
//  │    │   ├─ UsernamePasswordAuthenticationFilter：                   │   │
//  │    │   │  匹配 POST /api/auth/login → 执行表单登录认证             │   │
//  │    │   │  成功 → onAuthSuccess() 生成 JWT 返回                     │   │
//  │    │   └─ Security 内部链通过 → chain.doFilter()                   │   │
//  │    │                                                              │   │
//  │    │ index=2: （无更多 Filter）→ 到达 DispatcherServlet             │   │
//  │    └──────────────────────────────────────────────────────────────┘   │
//  │                                                                       │
//  │  层级 4：DispatcherServlet                                           │
//  │    HandlerMapping  → 匹配到 AuthorizeController.login()              │
//  │    HandlerAdapter  → 解析 @RequestBody 参数                          │
//  │    执行业务方法     → 返回 RestBean.success(token)                   │
//  │    写回 Response   → JSON 字符串写入 response body                   │
//  │                                                                       │
//  │  层级 5：响应回程                                                     │
//  │    Tomcat Connector 把 response 序列化 → HTTP 响应报文                │
//  │    浏览器收到 → 检查 CORS 头 → 通过 → 交给 JavaScript                │
//  └───────────────────────────────────────────────────────────────────────┘
//
//
//  ▐ 六、关键设计要点
//  ────────────────────────────────────────────────────────────────────────────
//
//  ① 为什么 CorsFilter 要在 Spring Security 之前？
//     OPTIONS 预检请求不带 JWT，如果先经过 Security 会被拦截返回 401，
//     浏览器收不到 CORS 头就会报跨域错误。
//     → 所以本类用 @Order(-102) 排在 Security(-100) 之前。
//
//  ② 为什么用 @Component 而不用 addFilterBefore？
//     addFilterBefore 是往 Security 内部链里插，已经晚了。
//     @Component 是往 Servlet 容器链里注册，才真正在 Security 之前。
//
//  ③ DispatcherServlet 和普通 Servlet 的区别？
//     普通 Servlet：一个 URL 一个 Servlet，if-else 分发。
//     DispatcherServlet：一个 Servlet 管所有，通过 HandlerMapping 分发。
//
//  ④ Spring Boot 做了什么？
//     内嵌容器 + 自动扫描 Filter + 自动创建 DispatcherServlet
//     + 自动注册 Spring Security = 零配置启动
// ============================================================================
//
//
// ▐ 七、Filter 体系全景图 —— 你能继承哪些 Filter？
// ─────────────────────────────────────────────────────────────────────────────
//
//  Servlet + Spring 中的 Filter 分三个层级：
//
//
//  ┌──────────────────────────────────────────────────────────────────────┐
//  │  层级 1：Servlet 规范原生（jakarta.servlet）                         │
//  │  ─────────────────────────────────────────────────────────────────── │
//  │                                                                      │
//  │  Filter  ← 最基础的接口，所有 Filter 的根                            │
//  │  ├─ void init(FilterConfig)                                          │
//  │  ├─ void doFilter(ServletRequest, ServletResponse, FilterChain)      │
//  │  └─ void destroy()                                                   │
//  │                                                                      │
//  │  HttpFilter  ← 本类 CorsFilter 用的，Filter 的 HTTP 版实现            │
//  │  └─ doFilter() 内部已经把 request/response 强转成                    │
//  │     HttpServletRequest / HttpServletResponse，你直接拿来用           │
//  │                                                                      │
//  ├──────────────────────────────────────────────────────────────────────┤
//  │  层级 2：Spring 封装（org.springframework.web.filter）               │
//  │  ─────────────────────────────────────────────────────────────────── │
//  │                                                                      │
//  │  OncePerRequestFilter  ← JwtAuthorizeFilter 用的                     │
//  │  └─ 保证一次请求只执行一次（避免 forward/include 重复调用）           │
//  │     你需要实现的是 doFilterInternal() 而不是 doFilter()              │
//  │     额外能力：shouldNotFilter() 可以跳过某些请求                     │
//  │                                                                      │
//  │  GenericFilterBean                                                    │
//  │  └─ 比 HttpFilter 多了个功能：能从 Spring 环境读取初始化参数          │
//  │     （通过 getFilterConfig()、getEnvironment() 等）                  │
//  │                                                                      │
//  │  Spring 提供的一系列现成 Filter（开箱即用，你只需声明 @Bean）：       │
//  │                                                                      │
//  │  ┌───────────────────────────────────────────────────────────────┐   │
//  │  │  Filter 类                      作用                           │   │
//  │  ├───────────────────────────────────────────────────────────────┤   │
//  │  │  CharacterEncodingFilter       设置请求/响应的字符编码         │   │
//  │  │  HiddenHttpMethodFilter        支持表单 _method 伪装 PUT/DELETE│   │
//  │  │  FormContentFilter             解析 PUT/PATCH 的表单数据      │   │
//  │  │  RequestContextFilter          把请求信息绑定到当前线程        │   │
//  │  │  CorsFilter (Spring 官方版)    另一种 CORS 实现方式            │   │
//  │  │  ShallowEtagHeaderFilter       根据响应内容生成 ETag 缓存头    │   │
//  │  │  CommonsRequestLoggingFilter   打印请求日志（URL/参数/体等）   │   │
//  │  │  RelativeRedirectFilter        把 302 转成 307（保持请求方法）│   │
//  │  └───────────────────────────────────────────────────────────────┘   │
//  │                                                                      │
//  ├──────────────────────────────────────────────────────────────────────┤
//  │  层级 3：Spring Security 内部（org.springframework.security.web.*）  │
//  │  ─────────────────────────────────────────────────────────────────── │
//  │                                                                      │
//  │  这些 Filter 不是通过 @Component + @Order 注册到 Servlet 容器的，     │
//  │  而是通过 SecurityConfiguration 里的 addFilterBefore/After/At 插入   │
//  │  到 Spring Security 内部的过滤器链中。                                │
//  │                                                                      │
//  │  常见的 Security 内置 Filter：                                        │
//  │                                                                      │
//  │  ┌───────────────────────────────────────────────────────────────┐   │
//  │  │  Filter 类                      作用                           │   │
//  │  ├───────────────────────────────────────────────────────────────┤   │
//  │  │  SecurityContextPersistenceFilter  从 Session/请求恢复上下文  │   │
//  │  │  UsernamePasswordAuthenticationFilter  处理表单登录           │   │
//  │  │  BasicAuthenticationFilter         HTTP Basic 认证            │   │
//  │  │  BearerTokenAuthenticationFilter   OAuth2 Bearer Token        │   │
//  │  │  RememberMeAuthenticationFilter    "记住我" Cookie 认证       │   │
//  │  │  AnonymousAuthenticationFilter     未登录用户赋予匿名角色     │   │
//  │  │  SessionManagementFilter           会话固定保护/并发控制      │   │
//  │  │  ExceptionTranslationFilter        认证/授权异常 → HTTP 状态码│   │
//  │  │  FilterSecurityInterceptor         最终执行 access() 决策     │   │
//  │  └───────────────────────────────────────────────────────────────┘   │
//  └──────────────────────────────────────────────────────────────────────┘
//
//
//  ▐ 八、三个 Filter 基类的选择决策树
//  ─────────────────────────────────────────────────────────────────────────────
//
//  你想拦截请求？
//    │
//    ├─ 需要在 Security 认证之前处理（如跨域、IP 限流、请求日志）？
//    │   → 继承 HttpFilter + @Component + @Order
//    │      例：本类 CorsFilter
//    │
//    ├─ 需要在 Security 内部链中插入认证/授权逻辑？
//    │   → 继承 OncePerRequestFilter + addFilterBefore/After()
//    │      例：JwtAuthorizeFilter
//    │
//    ├─ 和业务无关的通用 HTTP 处理？
//    │   → 继承 HttpFilter + @Component + @Order
//    │      例：请求耗时统计、安全响应头、URL 重写
//    │
//    ├─ 和业务有关、依赖 Spring 上下文、想用 shouldNotFilter()？
//    │   → 继承 OncePerRequestFilter + @Component
//    │      例：租户上下文注入、traceId 注入
//    │
//    └─ 想重用 Spring 现成的？
//        → 直接声明对应 Filter 的 @Bean
//          例：CharacterEncodingFilter、CommonsRequestLoggingFilter
//
//
//  ▐ 九、核心区别速查表
//  ─────────────────────────────────────────────────────────────────────────────
//
//  ┌──────────────────────┬──────────────────┬──────────────────────────────┐
//  │                      │ HttpFilter       │ OncePerRequestFilter         │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 所在包               │ jakarta.servlet  │ org.springframework.web      │
//  │                      │ .http            │ .filter                      │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 需要重写的方法       │ doFilter()       │ doFilterInternal()           │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 请求类型已转 HTTP?   │ ✅ 是            │ ✅ 是                        │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 保证一次请求只执行   │ ❌ 不保证        │ ✅ 保证                      │
//  │ 一次？               │ （forward 会      │ （通过额外 FilterChain 标记）│
//  │                      │  重复调用）       │                              │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 支持 shouldNotFilter?│ ❌ 不支持        │ ✅ 支持（可跳过某些路径）    │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 注册方式             │ @Component       │ @Component 或                │
//  │                      │ + @Order         │ addFilterBefore/After()      │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 注册层级             │ Servlet 容器链   │ 可注册到 Servlet 容器链      │
//  │                      │                  │ 或 Security 内部链            │
//  ├──────────────────────┼──────────────────┼──────────────────────────────┤
//  │ 适用场景             │ 通用跨域、编码   │ JWT 认证、安全相关逻辑       │
//  │                      │ 日志、限流等     │ 业务上下文等                  │
//  └──────────────────────┴──────────────────┴──────────────────────────────┘
// ============================================================================
