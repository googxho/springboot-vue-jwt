package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "借阅记录")
public class Borrow implements BaseData {
    @TableId(type = IdType.AUTO)
    @Schema(description = "借阅记录ID（自增主键，新增时无需填写）", example = "1")
    private Integer id;

    @Schema(description = "学生ID", example = "1001")
    private Integer sid;

    @Schema(description = "图书ID", example = "2001")
    private Integer bid;

    @Schema(description = "借阅时间", example = "2025-01-15 10:30:00")
    private Date time;

    @Schema(description = "书名（冗余字段，方便展示）", example = "三体")
    private String bookName;

    @Schema(description = "学生姓名（冗余字段，方便展示）", example = "张三")
    private String studentName;
}
