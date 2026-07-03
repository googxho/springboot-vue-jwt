package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 数据库中的用户信息
 */
@Data
@TableName("account")
@AllArgsConstructor
@Schema(description = "用户账户信息")
public class Account implements BaseData {
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID（自增主键）", example = "1")
    Integer sid;

    @Schema(description = "用户名", example = "zhangsan")
    String username;

    @Schema(description = "密码（BCrypt 加密存储）", example = "$2a$10$...")
    String password;

    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    String email;

    @Schema(description = "用户角色", example = "user", allowableValues = {"user", "admin"})
    String role;

    @Schema(description = "注册时间", example = "2025-01-01 12:00:00")
    Date registerTime;
}
