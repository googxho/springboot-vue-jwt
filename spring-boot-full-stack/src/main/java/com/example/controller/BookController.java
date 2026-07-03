package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Book;
import com.example.service.BookService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书管理接口。
 */
@RestController
@RequestMapping("/api/book")
public class BookController {

    @Resource
    private BookService bookService;

    /**
     * 查询全部图书。
     */
    @GetMapping("/list")
    public RestBean<List<Book>> list() {
        return RestBean.success(bookService.list());
    }

    /**
     * 根据ID查询图书。
     */
    @GetMapping("/{id}")
    public RestBean<Book> getById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return RestBean.failure(404, "图书不存在");
        }
        return RestBean.success(book);
    }

    /**
     * 新增图书。
     */
    @PostMapping("/add")
    public RestBean<Void> add(@RequestBody Book book) {
        bookService.save(book);
        return RestBean.success();
    }

    /**
     * 更新图书。
     */
    @PutMapping("/update")
    public RestBean<Void> update(@RequestBody Book book) {
        if (bookService.updateById(book)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "图书不存在");
    }

    /**
     * 删除图书。
     */
    @DeleteMapping("/{id}")
    public RestBean<Void> delete(@PathVariable Integer id) {
        if (bookService.removeById(id)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "图书不存在");
    }
}
