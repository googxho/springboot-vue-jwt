package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * 认证校验相关接口，包含用户注册、密码重置等操作。
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录校验相关", description = "用户注册、密码重置、邮件验证码等认证相关接口")
public class AuthorizeController {

    @Resource
    AccountService accountService;

    /**
     * 请求邮件验证码
     *
     * @param email   接收验证码的邮箱地址
     * @param type    验证码类型：register（注册）或 reset（重置密码）
     * @param request HTTP 请求对象（用于获取客户端 IP）
     * @return 是否请求成功
     */
    @GetMapping("/ask-code")
    @Operation(summary = "发送邮件验证码", description = "向指定邮箱发送验证码，type 为 register 表示注册验证码，reset 表示密码重置验证码")
    public RestBean<Void> askVerifyCode(
            @RequestParam @Email
            @Parameter(description = "接收验证码的邮箱地址", required = true, example = "user@example.com") String email,
            @RequestParam @Pattern(regexp = "(register|reset)")
            @Parameter(description = "验证码类型：register=注册验证码，reset=密码重置验证码", required = true, example = "register") String type,
            HttpServletRequest request) {
        return this.messageHandle(() ->
                accountService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    /**
     * 进行用户注册操作，需要先请求邮件验证码
     *
     * @param vo 注册信息（邮箱、验证码、用户名、密码）
     * @return 是否注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用邮箱验证码完成新用户注册，需先调用 /ask-code 获取验证码")
    public RestBean<Void> register(@RequestBody @Valid
                                   @Parameter(description = "注册表单信息", required = true) EmailRegisterVO vo) {
        return this.messageHandle(() ->
                accountService.registerEmailAccount(vo));
    }

    /**
     * 执行密码重置确认，检查验证码是否正确
     *
     * @param vo 密码重置确认信息（邮箱、验证码）
     * @return 是否操作成功
     */
    @PostMapping("/reset-confirm")
    @Operation(summary = "验证重置密码的验证码", description = "校验用户输入的邮箱验证码是否正确，验证通过后才能进行密码重置")
    public RestBean<Void> resetConfirm(@RequestBody @Valid
                                       @Parameter(description = "验证码确认信息（邮箱+验证码）", required = true) ConfirmResetVO vo) {
        return this.messageHandle(() -> accountService.resetConfirm(vo));
    }

    /**
     * 执行密码重置操作
     *
     * @param vo 密码重置信息（邮箱、验证码、新密码）
     * @return 是否操作成功
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "验证码校验通过后，使用新密码重置账户密码")
    public RestBean<Void> resetPassword(@RequestBody @Valid
                                        @Parameter(description = "密码重置表单（邮箱+验证码+新密码）", required = true) EmailResetVO vo) {
        return this.messageHandle(() ->
                accountService.resetEmailAccountPassword(vo));
    }

    /**
     * 针对于返回值为String作为错误信息的方法进行统一处理
     * @param action 具体操作
     * @return 响应结果
     * @param <T> 响应结果类型
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action){
        String message = action.get();
        if(message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }
}
