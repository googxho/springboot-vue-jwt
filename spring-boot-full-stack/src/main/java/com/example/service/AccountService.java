package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户认证服务接口 —— 定义用户注册、登录、密码重置等操作契约。
 * <p>
 * 继承体系：
 * <pre>
 *   {@code IService<Account>}    — MyBatis-Plus 通用 Service 接口，提供 save/update/query 等基础方法
 *   {@code UserDetailsService}   — Spring Security 用户加载接口，用于登录认证
 * </pre>
 */
public interface AccountService extends IService<Account>, UserDetailsService {

    /**
     * 通过用户名或邮件地址查找用户
     * @param text 用户名或邮件
     * @return 账户实体
     */
    Account findAccountByNameOrEmail(String text);

    /**
     * 生成注册验证码存入Redis中，并将邮件发送请求提交到消息队列等待发送
     * @param type    类型（register / reset）
     * @param email   邮件地址
     * @param address 请求IP地址
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String registerEmailVerifyCode(String type, String email, String address);

    /**
     * 邮件验证码注册账号操作，需要检查验证码是否正确以及邮箱、用户名是否存在重名
     * @param info 注册基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String registerEmailAccount(EmailRegisterVO info);

    /**
     * 邮件验证码重置密码操作，需要检查验证码是否正确
     * @param info 重置基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String resetEmailAccountPassword(EmailResetVO info);

    /**
     * 重置密码确认操作，验证验证码是否正确
     * @param info 验证基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    String resetConfirm(ConfirmResetVO info);
}
