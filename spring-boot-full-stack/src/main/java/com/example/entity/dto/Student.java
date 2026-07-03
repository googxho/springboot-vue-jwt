package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "学生信息")
public class Student implements BaseData {
    @TableId(type = IdType.AUTO)
    @Schema(description = "学生ID（自增主键，新增时无需填写）", example = "1")
    private Integer id;

    @Schema(description = "学生姓名", example = "张三")
    private String name;

    @Schema(description = "性别", example = "男", allowableValues = {"男", "女"})
    private String sex;

    @Schema(description = "年级", example = "2024级")
    private String grade;
}
