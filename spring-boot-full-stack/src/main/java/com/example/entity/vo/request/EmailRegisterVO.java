package com.example.entity.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册表单信息
 */
@Data
@Schema(description = "用户注册请求")
public class EmailRegisterVO {
    @Email
    @Schema(description = "邮箱地址", required = true, example = "user@example.com")
    String email;

    @Length(max = 6, min = 6)
    @Schema(description = "6位邮箱验证码", required = true, example = "123456", minLength = 6, maxLength = 6)
    String code;

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1, max = 10)
    @Schema(description = "用户名（支持中英文、数字，1-10个字符）", required = true, example = "张三")
    String username;

    @Length(min = 6, max = 20)
    @Schema(description = "登录密码（6-20个字符）", required = true, example = "abc123456")
    String password;
}
