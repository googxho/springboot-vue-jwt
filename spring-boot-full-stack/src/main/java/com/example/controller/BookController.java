package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Book;
import com.example.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书管理接口，支持图书的增删改查。
 */
@RestController
@RequestMapping("/api/book")
@Tag(name = "图书管理", description = "图书的查询、新增、修改、删除接口")
public class BookController {

    @Resource
    private BookService bookService;

    /**
     * 查询全部图书。
     */
    @GetMapping("/list")
    @Operation(summary = "查询全部图书", description = "返回系统中所有图书的列表")
    public RestBean<List<Book>> list() {
        return RestBean.success(bookService.list());
    }

    /**
     * 根据ID查询图书。
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询图书", description = "根据图书ID查询单本图书的详细信息")
    public RestBean<Book> getById(
            @PathVariable
            @Parameter(description = "图书ID", required = true, example = "1") Integer id) {
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
    @Operation(summary = "新增图书", description = "添加一本新图书到系统中")
    public RestBean<Void> add(@RequestBody
                              @Parameter(description = "图书信息（无需填ID，由系统自动生成）", required = true) Book book) {
        bookService.save(book);
        return RestBean.success();
    }

    /**
     * 更新图书。
     */
    @PutMapping("/update")
    @Operation(summary = "更新图书", description = "根据图书ID更新已有图书的信息")
    public RestBean<Void> update(@RequestBody
                                 @Parameter(description = "需要更新的图书信息（ID必填）", required = true) Book book) {
        if (bookService.updateById(book)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "图书不存在");
    }

    /**
     * 删除图书。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除图书", description = "根据图书ID删除指定图书")
    public RestBean<Void> delete(
            @PathVariable
            @Parameter(description = "要删除的图书ID", required = true, example = "1") Integer id) {
        if (bookService.removeById(id)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "图书不存在");
    }
}
