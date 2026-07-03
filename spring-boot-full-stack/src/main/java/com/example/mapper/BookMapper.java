package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Book;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书表（book）的数据访问层。
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
