package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表（account）的数据访问层（Mapper）。
 * <p>
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  作用：        操作数据库中的 account 表                          │
 * │  技术：        MyBatis-Plus                                        │
 * │  继承：        BaseMapper<Account> → 自带全套 CRUD 方法            │
 * │  注册：        @Mapper → MyBatis 自动扫描并创建代理对象             │
 * └─────────────────────────────────────────────────────────────────────┘
 * <p>
 * 继承 BaseMapper<Account> 后自动获得以下方法（无需写 SQL）：
 * <p>
 * ┌──────────────────────┬──────────────────────────────────────────┐
 * │ 方法                  │ 作用                                     │
 * ├──────────────────────┼──────────────────────────────────────────┤
 * │ insert(account)      │ 插入一条用户记录                         │
 * │ deleteById(id)       │ 根据主键删除                             │
 * │ updateById(account)  │ 根据主键更新                             │
 * │ selectById(id)       │ 根据主键查询                             │
 * │ selectOne(wrapper)   │ 按条件查询单条记录                       │
 * │ selectList(wrapper)  │ 按条件查询多条记录                       │
 * │ selectCount(wrapper) │ 按条件统计数量                           │
 * │ ...                  │ 更多见 BaseMapper 文档                   │
 * └──────────────────────┴──────────────────────────────────────────┘
 * <p>
 * 如果默认方法不够用（比如复杂联表查询），可以在此接口中自定义方法，
 * 然后在 resources/mapper/ 下编写对应的 XML 映射文件。
 * <p>
 * 关联的实体类：
 *   ┌─ Account ────────────────────────────────────────────┐
 *   │  @TableName("account")  → 映射到 account 表     │
 *   │  @TableId(type = IdType.AUTO) → sid 字段自增主键       │
 *   │  字段: sid, username, password, email, role, regTime   │
 *   └──────────────────────────────────────────────────────┘
 */
@Mapper  // 告诉 MyBatis：这是一个 Mapper 接口，启动时自动生成代理实现类
public interface AccountMapper extends BaseMapper<Account> {

    // ──────────────────────────────────────────────────────────────
    // 这里不需要写任何方法！BaseMapper 已经提供了全套单表 CRUD。
    //
    // 如果要加自定义查询（如：根据邮箱和状态联合查询），可以这样写：
    //
    //   @Select("SELECT * FROM account WHERE email = #{email}")
    //   Account findAccountByEmail(String email);
    //
    // 或者在 resources/mapper/AccountMapper.xml 中写 SQL。
    // ──────────────────────────────────────────────────────────────
}
