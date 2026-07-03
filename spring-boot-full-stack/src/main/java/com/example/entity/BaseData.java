package com.example.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 用于DTO快速转换VO实现，只需将DTO类继承此类即可使用
 */
public interface BaseData {
    /**
     * 创建指定的VO类并将当前DTO对象中的所有成员变量值直接复制到VO对象中
     * @param clazz 指定VO类型
     * @param consumer 返回VO对象之前可以使用Lambda进行额外处理
     * @return 指定VO对象
     * @param <V> 指定VO类型
     */
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    /**
     * 创建指定的VO类并将当前DTO对象中的所有成员变量值直接复制到VO对象中
     * @param clazz 指定VO类型
     * @return 指定VO对象
     * @param <V> 指定VO类型
     */
    default <V> V asViewObject(Class<V> clazz) {
        try {
            // getDeclaredFields()：拿到 AuthorizeVO 声明的"所有"字段（包括 private 的）
            // 注意不是 getFields()——getFields() 只能拿到 public 字段
            Field[] fields = clazz.getDeclaredFields();

            // getConstructor()：拿到 AuthorizeVO 的"无参构造器"
            // 因为 AuthorizeVO 没写构造器，Java 会默认生成一个隐藏的无参构造器
            // 反射能找到它，然后用 newInstance() 调用它 → 相当于 new AuthorizeVO()
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            Arrays.asList(fields).forEach(field -> convert(field, v));
            return v;
        } catch (ReflectiveOperationException exception) {
            Logger logger = LoggerFactory.getLogger(BaseData.class);
            logger.error("在VO与DTO转换时出现了一些错误", exception);
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * 内部使用，快速将当前类中目标对象字段同名字段的值复制到目标对象字段上
     * @param field 目标对象字段
     * @param target 目标对象
     */
    private void convert(Field field, Object target){
        try {
            Field source = this.getClass().getDeclaredField(field.getName());
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(target, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
    }
}

/*
 * ═══════════════════════════════════════════════════════════════════════
 *  BaseData 详解 —— 用具体例子说明它在干什么
 * ═══════════════════════════════════════════════════════════════════════
 *
 * ───────────────────────────────────────────────────────────────────────
 *  ▐ 场景：从数据库查出 User，要返回给前端
 * ───────────────────────────────────────────────────────────────────────
 *
 *  【数据库表】account
 *    sid  |  username  |  password  |  email  |  role  |  register_time
 *
 *  【实体类】Account（DTO，直接映射数据库）
 *    class Account {
 *        Integer sid;          // 数据库有
 *        String username;     // 数据库有
 *        String password;     // 数据库有  ← 这是敏感信息！
 *        String email;        // 数据库有
 *        String role;         // 数据库有
 *        Date registerTime;   // 数据库有
 *    }
 *
 *  【响应类】AuthorizeVO（VO，返回给前端）
 *    class AuthorizeVO {
 *        String username;     // 前端需要
 *        String role;         // 前端需要
 *        String token;        // 前端需要  ← 这是登录后生成的，数据库没有
 *        Date expire;         // 前端需要  ← 这是登录后生成的，数据库没有
 *    }
 *
 *  问题：Account 有 6 个字段，AuthorizeVO 有 4 个字段，
 *       其中 username、role 是同名的，password、email、registerTime 在 VO 里没有。
 *       如果手动写转换代码，你要写好几行 get/set：
 *
 *          AuthorizeVO vo = new AuthorizeVO();
 *          vo.setUsername(account.getUsername());
 *          vo.setRole(account.getRole());
 *          vo.setToken(jwt);
 *          vo.setExpire(expire);
 *
 *  BaseData 就是想让你省掉这些重复劳动——只复制同名字段，剩下的你手动设。
 *
 * ───────────────────────────────────────────────────────────────────────
 *  ▐ 使用 BaseData 后，上面的代码变成一行：
 * ───────────────────────────────────────────────────────────────────────
 *
 *    // Account implements BaseData，所以 account 拥有 asViewObject() 方法
 *    AuthorizeVO vo = account.asViewObject(AuthorizeVO.class, o -> {
 *        o.setToken(jwt);       // token 在 Account 里没有同名 → 手动设
 *        o.setExpire(expire);   // expire 在 Account 里没有同名 → 手动设
 *    });
 *
 *  效果一样，但不必自己写 username 和 role 的赋值。
 *
 * ───────────────────────────────────────────────────────────────────────
 *  ▐ 核心原理：反射（Reflection）⭐
 * ───────────────────────────────────────────────────────────────────────
 *
 *  asViewObject() 的实现思路：
 *
 *  1. 拿到 AuthorizeVO 的类信息（Class<AuthorizeVO>）
 *     ↓
 *  2. 通过反射获取它的无参构造器，创建一个空对象
 *     AuthorizeVO vo = new AuthorizeVO();   ← 反射版的 new
 *     ↓
 *  3. 获取 AuthorizeVO 声明的所有字段：[username, role, token, expire]
 *     ↓
 *  4. 遍历每个字段，对每个字段执行 convert()：
 *     ├─ 去 Account 类里找 "同名" 的字段
 *     │    username → Account 里有 → 把 account.username 的值复制给 vo.username
 *     │    role     → Account 里有 → 把 account.role 的值复制给 vo.role
 *     │    token    → Account 里没有 → 什么也不做（忽略）
 *     │    expire   → Account 里没有 → 什么也不做（忽略）
 *     └─ 所以 vo 被填好了 username 和 role
 *     ↓
 *  5. 返回 vo，然后在 Lambda 里手动设 token 和 expire
 *
 *  整个过程的核心就是这个 convert() 方法：
 *
 *    private void convert(Field targetField, Object targetObject) {
 *        // 在当前对象（account）的类中找同名同类型的字段
 *        Field sourceField = this.getClass().getDeclaredField(targetField.getName());
 *
 *        // 因为字段都是 private 的，反射默认不能访问，
 *        // 调用 setAccessible(true) 强行打开访问权限
 *        targetField.setAccessible(true);
 *        sourceField.setAccessible(true);
 *
 *        // 把当前对象的 sourceField 的值，赋给目标对象的 targetField
 *        targetField.set(targetObject, sourceField.get(this));
 *        //                                    └─ this 就是 account
 *        //              └─ 相当于 vo.username = account.username
 *    }
 *
 *  用大白话说就是：
 *    "看目标对象有哪些字段，在源对象里找同名的，找到了就复制过去。"
 *
 * ───────────────────────────────────────────────────────────────────────
 *  ▐ 为什么要有两个 asViewObject 重载？
 * ───────────────────────────────────────────────────────────────────────
 *
 *    asViewObject(Class<V> clazz)
 *      → 只做字段复制，不做额外处理
 *      → 适用：两个类的字段完全对应（如 Account → AccountVO）
 *
 *    asViewObject(Class<V> clazz, Consumer<V> consumer)
 *      → 先做字段复制，再执行 Lambda 做额外设置
 *      → 适用：VO 里有些字段在 DTO 里没有，需要手动补充（如 token、expire）
 *      → Consumer 是一个"消费型"函数接口：给你一个 VO 对象，你往里设值，它没有返回值
 *
 * ───────────────────────────────────────────────────────────────────────
 *  ▐ 为什么用接口 + default 方法？
 * ───────────────────────────────────────────────────────────────────────
 *
 *  interface BaseData 加 default 方法，而不是用抽象类：
 *
 *    Java 类只能继承一个父类，但可以实现多个接口。
 *    如果 Account 已经继承了某个类（比如 MyBatis-Plus 的 Model），
 *    它就不能再继承 BaseData 了。
 *
 *    用接口 + default 方法，Account 只需多写一个 implements BaseData，
 *    就能免费获得 asViewObject() 方法，不影响它的继承关系。
 */
