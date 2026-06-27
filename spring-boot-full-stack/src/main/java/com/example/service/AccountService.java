package com.example.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务 —— 从数据库加载用户信息供 Spring Security 认证使用。
 * <p>
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  职责：        实现 UserDetailsService 接口                        │
 * │  作用：        Spring Security 登录时自动调用此类查询数据库         │
 * │  数据表：      db_account（用户表）                                │
 * │  密码：        数据库中存储的是 BCrypt 加密后的密码                │
 * └─────────────────────────────────────────────────────────────────────┘
 * <p>
 * 认证时的调用链：
 * <p>
 *   POST /api/auth/login
 *     │
 *     ▼
 *   UsernamePasswordAuthenticationFilter（拦截登录请求）
 *     │
 *     ▼
 *   AuthenticationManager.authenticate()
 *     │
 *     ▼
 *   ★ AccountService.loadUserByUsername()       ← 本类
 *     │
 *     ├─ AccountMapper 查数据库（用户名 / 邮箱）
 *     ├─ 返回 UserDetails（用户名、加密密码、角色）
 *     │
 *     ▼
 *   BCryptPasswordEncoder 比对密码
 *     │
 *     ├─ 匹配 → 认证成功 → 调用 onAuthSuccess()
 *     └─ 不匹配 → 认证失败 → 调用 onAuthFailure()
 */
@Service  // 注册为 Spring 服务 Bean，Spring Security 会自动发现这个 UserDetailsService
public class AccountService implements UserDetailsService {

    // MyBatis-Plus Mapper，注入后可直接操作 db_account 表
    @Resource
    AccountMapper mapper;

    /**
     * 根据用户名或邮箱加载用户信息（Spring Security 认证时自动调用）。
     * <p>
     * 支持两种登录方式：
     * <ul>
     *   <li>用户名登录：输入 username</li>
     *   <li>邮箱登录：输入 email</li>
     * </ul>
     * 优先按用户名查找，没找到再按邮箱查找。
     * <p>
     * 流程：
     * <pre>
     *   ① 查 db_account WHERE username = 输入值
     *   ② 没找到 → 查 db_account WHERE email = 输入值
     *   ③ 都没找到 → 抛 UsernameNotFoundException
     *   ④ 找到 → 封装成 UserDetails 返回
     * </pre>
     *
     * @param username 前端提交的用户名（也可能是邮箱地址）
     * @return Spring Security 的 UserDetails（包含用户名、密码、角色）
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 第一步：按用户名查找
        Account account = mapper.selectOne(
                Wrappers.<Account>lambdaQuery().eq(Account::getUsername, username));

        // 第二步：没找到，按邮箱查找（支持邮箱登录）
        if (account == null) {
            account = mapper.selectOne(
                    Wrappers.<Account>lambdaQuery().eq(Account::getEmail, username));
        }

        // 第三步：都没找到，抛出异常
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 第四步：将数据库 Account 转为 Spring Security 的 UserDetails
        // withUsername() → 设置用户名
        // password()    → 设置数据库中的加密密码（BCrypt）
        // roles()       → 设置用户角色（如 "user"、"admin"）
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /**
     * 根据用户名查询完整的 Account 对象。
     * <p>
     * 这个方法用于登录成功后获取用户的额外信息（如 ID）。
     * 在 onAuthSuccess() 中调用，用于生成包含 userId 的 JWT。
     *
     * @param username 用户名
     * @return Account 完整用户对象，查不到返回 null
     */
    public Account findAccountByUsername(String username) {
        return mapper.selectOne(
                Wrappers.<Account>lambdaQuery().eq(Account::getUsername, username));
    }
}
