package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Book;
import com.example.mapper.BookMapper;
import com.example.service.BookService;
import org.springframework.stereotype.Service;

/**
 * 图书管理服务实现。
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
}
