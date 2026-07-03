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

import java.math.BigDecimal;

/**
 * 图书实体，映射 book 表。
 */
@Data
@TableName("book")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图书信息")
public class Book implements BaseData {
    @TableId(type = IdType.AUTO)
    @Schema(description = "图书ID（自增主键，新增时无需填写）", example = "1")
    private Integer id;

    @Schema(description = "书名", example = "三体")
    private String title;

    @TableField("`desc`")
    @Schema(description = "图书简介", example = "刘慈欣创作的科幻小说")
    private String desc;

    @Schema(description = "图书价格（元）", example = "39.90")
    private BigDecimal price;
}
