package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Borrow;
import com.example.service.BorrowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * 借阅管理接口。
 */
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Resource
    private BorrowService borrowService;

    /**
     * 查询全部借阅记录。
     */
    @GetMapping("/list")
    public RestBean<List<Borrow>> list() {
        return RestBean.success(borrowService.list());
    }

    /**
     * 根据ID查询借阅记录。
     */
    @GetMapping("/{id}")
    public RestBean<Borrow> getById(@PathVariable Integer id) {
        Borrow borrow = borrowService.getById(id);
        if (borrow == null) {
            return RestBean.failure(404, "借阅记录不存在");
        }
        return RestBean.success(borrow);
    }

    /**
     * 借书。
     */
    @PostMapping("/borrow")
    public RestBean<Void> borrow(@RequestBody Borrow borrow) {
        return messageHandle(() -> borrowService.borrowBook(borrow));
    }

    /**
     * 还书。
     */
    @DeleteMapping("/return/{id}")
    public RestBean<Void> returnBook(@PathVariable Integer id) {
        return messageHandle(() -> borrowService.returnBook(id));
    }

    /**
     * 统一错误处理：Service 返回 null 表示成功，返回字符串即为错误信息。
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }
}
