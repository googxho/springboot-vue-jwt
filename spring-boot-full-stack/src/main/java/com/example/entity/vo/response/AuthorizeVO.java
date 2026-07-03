package com.example.entity.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 登录验证成功的用户信息响应
 */
@Data
@Schema(description = "登录成功响应")
public class AuthorizeVO {
    @Schema(description = "用户名", example = "zhangsan")
    String username;

    @Schema(description = "用户角色", example = "user", allowableValues = {"user", "admin"})
    String role;

    @Schema(description = "JWT 访问令牌，后续请求需放在 Authorization 头中", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token;

    @Schema(description = "令牌过期时间", example = "2025-01-02 12:00:00")
    Date expire;
}
