package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生实体，映射 student 表。
 */
@Data
@TableName("student")
@NoArgsConstructor
@AllArgsConstructor
public class Student implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer id;    // 学生ID
    private String name;   // 姓名
    private String sex;    // 性别
    private String grade;  // 年级
}
