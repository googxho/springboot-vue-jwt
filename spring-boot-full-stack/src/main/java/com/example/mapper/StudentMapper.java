package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生表（student）的数据访问层。
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
