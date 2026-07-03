package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 图书实体，映射 book 表。
 */
@Data
@TableName("book")
@NoArgsConstructor
@AllArgsConstructor
public class Book implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer id;       // 图书ID
    private String title;     // 书名
    @TableField("`desc`")
    private String desc;      // 简介（desc 是 MySQL 保留关键字，需反引号转义）
    private BigDecimal price; // 价格
}
