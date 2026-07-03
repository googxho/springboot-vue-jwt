package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 借阅记录实体，映射 borrow 表。
 */
@Data
@TableName("borrow")
@NoArgsConstructor
@AllArgsConstructor
public class Borrow implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer id;          // 借阅ID
    private Integer sid;         // 学生ID
    private Integer bid;         // 图书ID
    private Date time;           // 借阅时间
    private String bookName;     // 书名（冗余字段，列名是驼峰）
    private String studentName;  // 学生姓名（冗余字段，列名是驼峰）
}
