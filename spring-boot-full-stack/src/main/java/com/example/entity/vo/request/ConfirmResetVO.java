package com.example.entity.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 密码重置确认请求 —— 校验邮箱验证码是否正确。
 */
@Data
@AllArgsConstructor
@Schema(description = "密码重置确认请求")
public class ConfirmResetVO {
    @Email
    @Schema(description = "邮箱地址", required = true, example = "user@example.com")
    String email;

    @Length(max = 6, min = 6)
    @Schema(description = "6位邮箱验证码", required = true, example = "123456", minLength = 6, maxLength = 6)
    String code;
}
