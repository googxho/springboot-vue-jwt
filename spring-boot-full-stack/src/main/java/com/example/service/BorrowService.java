package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Borrow;

/**
 * 借阅管理服务接口。
 */
public interface BorrowService extends IService<Borrow> {

    /**
     * 借书：创建借阅记录，同时填充冗余字段 bookName 和 studentName。
     * @param borrow 借阅记录
     * @return 错误信息，null 表示成功
     */
    String borrowBook(Borrow borrow);

    /**
     * 还书：根据借阅ID删除记录。
     * @param borrowId 借阅ID
     * @return 错误信息，null 表示成功
     */
    String returnBook(Integer borrowId);
}
