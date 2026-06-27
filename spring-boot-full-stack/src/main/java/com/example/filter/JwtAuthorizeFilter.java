package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 授权过滤器 —— 每次请求都会经过这里。
 * <p>
 * 核心职责：从请求头中提取 JWT 令牌，解析出用户信息并设置到 SecurityContext 中。
 * <p>
 * 工作流程：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  1. 从请求头 "Authorization" 中获取 Token（格式: "Bearer xxx"）│
 * │  2. 调用 JwtUtils.resolveJwt() 解析并验证 Token 是否有效        │
 * │  3. 如果 Token 有效：                                           │
 * │     ├─ 从 Token 中提取用户信息（用户名、角色等）                 │
 * │     ├─ 创建 UsernamePasswordAuthenticationToken（已认证状态）    │
 * │     ├─ 设置到 SecurityContextHolder 中（标记为"已登录"）        │
 * │     └─ 将用户 ID 存入 request 属性，方便 Controller 获取        │
 * │  4. 无论 Token 是否有效，都放行到下一个过滤器/控制器            │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Component  // 注册为 Spring Bean，Spring 会自动管理它的生命周期
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    // JWT 工具类，负责 Token 的解析和验证
    @Resource
    JwtUtils utils;

    /**
     * 核心过滤方法 —— 每次 HTTP 请求都会执行此方法（且仅执行一次）。
     * <p>
     * 为什么继承 OncePerRequestFilter 而不是直接实现 Filter？
     * 因为在 Servlet 容器的"转发(forward)"和"包含(include)"场景下，
     * 一个请求可能会被多个 Servlet 处理，导致过滤器被重复调用。
     * OncePerRequestFilter 确保同一请求生命周期内只过滤一次，避免
     * 重复认证或数据污染。
     *
     * @param request     HTTP 请求（可从中获取请求头、参数等）
     * @param response    HTTP 响应（可向客户端写回数据）
     * @param filterChain 过滤器链（调用 doFilter 放行到下一个过滤器/目标资源）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // ===== 第 1 步：从请求头中提取 JWT =====
        // 前端在登录后，会把 JWT 存放在请求头中：
        //   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
        String authorization = request.getHeader("Authorization");

        // ===== 第 2 步：解析并验证 JWT =====
        // 如果 Token 无效或已过期，resolveJwt 返回 null
        DecodedJWT jwt = utils.resolveJwt(authorization);

        // ===== 第 3 步：如果 Token 有效，设置认证信息 =====
        if (jwt != null) {
            // 从 JWT 中提取用户信息（用户名、角色列表等）
            UserDetails user = utils.toUser(jwt);

            // 创建一个"已认证"的令牌对象
            // 参数说明：
            //   principal   → 用户主体（UserDetails 对象）
            //   credentials → 凭证（用 JWT 就不需要密码了，传 null）
            //   authorities → 用户权限/角色列表
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            // 补充请求的详细信息（IP、SessionId 等）到认证对象中
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // ★ 关键一步：将认证信息设置到 SecurityContext 中
            // 这一步之后，Spring Security 就认为当前请求是"已登录"状态
            // 后续代码可以通过 SecurityContextHolder.getContext().getAuthentication() 获取用户信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将用户 ID 存入 request 属性，方便 Controller 层直接获取
            request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
        }
        // ===== 第 4 步：无论是否认证，都放行 =====
        // 注意：这里没有拦截逻辑——即使 JWT 无效，请求也会继续往下走。
        // 真正的拦截发生在 SecurityConfiguration 的 .anyRequest().authenticated()
        // 如果请求路径需要认证但 SecurityContext 中没有认证信息，Spring Security 会返回 401/403
        filterChain.doFilter(request, response);
    }
}
