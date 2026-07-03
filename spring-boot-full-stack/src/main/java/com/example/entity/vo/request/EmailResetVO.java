package com.example.entity.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 密码重置请求 —— 验证码校验通过后，提交新密码完成重置。
 */
@Data
@Schema(description = "密码重置请求")
public class EmailResetVO {
    @Email
    @Schema(description = "邮箱地址", required = true, example = "user@example.com")
    String email;

    @Length(max = 6, min = 6)
    @Schema(description = "6位邮箱验证码", required = true, example = "123456", minLength = 6, maxLength = 6)
    String code;

    @Length(min = 6, max = 20)
    @Schema(description = "新密码（6-20个字符）", required = true, example = "newPassword123")
    String password;
}
