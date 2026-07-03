package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Borrow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借阅表（borrow）的数据访问层。
 */
@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {
}
