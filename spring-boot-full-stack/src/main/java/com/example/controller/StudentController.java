package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Student;
import com.example.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理接口。
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * 查询全部学生。
     */
    @GetMapping("/list")
    public RestBean<List<Student>> list() {
        return RestBean.success(studentService.list());
    }

    /**
     * 根据ID查询学生。
     */
    @GetMapping("/{id}")
    public RestBean<Student> getById(@PathVariable Integer id) {
        Student student = studentService.getById(id);
        if (student == null) {
            return RestBean.failure(404, "学生不存在");
        }
        return RestBean.success(student);
    }

    /**
     * 新增学生。
     */
    @PostMapping("/add")
    public RestBean<Void> add(@RequestBody Student student) {
        studentService.save(student);
        return RestBean.success();
    }

    /**
     * 更新学生。
     */
    @PutMapping("/update")
    public RestBean<Void> update(@RequestBody Student student) {
        if (studentService.updateById(student)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "学生不存在");
    }

    /**
     * 删除学生。
     */
    @DeleteMapping("/{id}")
    public RestBean<Void> delete(@PathVariable Integer id) {
        if (studentService.removeById(id)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "学生不存在");
    }
}
