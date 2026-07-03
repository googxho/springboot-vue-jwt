package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Borrow;
import com.example.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * 借阅管理接口，支持借阅记录的查询、借书与还书操作。
 */
@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "图书借阅记录的查询、借书、还书接口")
public class BorrowController {

    @Resource
    private BorrowService borrowService;

    /**
     * 查询全部借阅记录。
     */
    @GetMapping("/list")
    @Operation(summary = "查询全部借阅记录", description = "返回系统中所有借阅记录的列表，包含借书人、书名、借阅时间等信息")
    public RestBean<List<Borrow>> list() {
        return RestBean.success(borrowService.list());
    }

    /**
     * 根据ID查询借阅记录。
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询借阅记录", description = "根据借阅记录ID查询单条借阅详情")
    public RestBean<Borrow> getById(
            @PathVariable
            @Parameter(description = "借阅记录ID", required = true, example = "1") Integer id) {
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
    @Operation(summary = "借阅图书", description = "创建一条借阅记录，记录学生借书的操作")
    public RestBean<Void> borrow(@RequestBody
                                 @Parameter(description = "借阅信息（需提供学生ID sid、图书ID bid）", required = true) Borrow borrow) {
        return messageHandle(() -> borrowService.borrowBook(borrow));
    }

    /**
     * 还书。
     */
    @DeleteMapping("/return/{id}")
    @Operation(summary = "归还图书", description = "根据借阅记录ID归还图书，删除该借阅记录")
    public RestBean<Void> returnBook(
            @PathVariable
            @Parameter(description = "借阅记录ID", required = true, example = "1") Integer id) {
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
