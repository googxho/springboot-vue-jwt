package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Book;
import com.example.entity.dto.Borrow;
import com.example.entity.dto.Student;
import com.example.mapper.BookMapper;
import com.example.mapper.BorrowMapper;
import com.example.mapper.StudentMapper;
import com.example.service.BorrowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 借阅管理服务实现。
 */
@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowMapper, Borrow> implements BorrowService {

    @Resource
    private BookMapper bookMapper;

    @Resource
    private StudentMapper studentMapper;

    @Override
    @Transactional
    public String borrowBook(Borrow borrow) {
        // 校验学生是否存在
        Student student = studentMapper.selectById(borrow.getSid());
        if (student == null) {
            return "学生不存在";
        }
        // 校验图书是否存在
        Book book = bookMapper.selectById(borrow.getBid());
        if (book == null) {
            return "图书不存在";
        }
        // 填充冗余字段
        borrow.setStudentName(student.getName());
        borrow.setBookName(book.getTitle());
        borrow.setTime(new Date());
        this.save(borrow);
        return null;
    }

    @Override
    public String returnBook(Integer borrowId) {
        Borrow borrow = this.getById(borrowId);
        if (borrow == null) {
            return "借阅记录不存在";
        }
        this.removeById(borrowId);
        return null;
    }
}
