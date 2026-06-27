package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * 登录验证成功的用户信息响应
 */
@Data
public class AuthorizeVO {
    String username;  // 用户名
    String role;      // 角色
    String token;     // JWT令牌
    Date expire;      // 令牌过期时间
}
