package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Student;
import com.example.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理接口，支持学生信息的增删改查。
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "学生管理", description = "学生信息的查询、新增、修改、删除接口")
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * 查询全部学生。
     */
    @GetMapping("/list")
    @Operation(summary = "查询全部学生", description = "返回系统中所有学生的列表")
    public RestBean<List<Student>> list() {
        return RestBean.success(studentService.list());
    }

    /**
     * 根据ID查询学生。
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询学生", description = "根据学生ID查询单个学生的详细信息")
    public RestBean<Student> getById(
            @PathVariable
            @Parameter(description = "学生ID", required = true, example = "1") Integer id) {
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
    @Operation(summary = "新增学生", description = "添加一名新学生到系统中")
    public RestBean<Void> add(@RequestBody
                              @Parameter(description = "学生信息（无需填ID，由系统自动生成）", required = true) Student student) {
        studentService.save(student);
        return RestBean.success();
    }

    /**
     * 更新学生。
     */
    @PutMapping("/update")
    @Operation(summary = "更新学生", description = "根据学生ID更新已有学生的信息")
    public RestBean<Void> update(@RequestBody
                                 @Parameter(description = "需要更新的学生信息（ID必填）", required = true) Student student) {
        if (studentService.updateById(student)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "学生不存在");
    }

    /**
     * 删除学生。
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除学生", description = "根据学生ID删除指定学生")
    public RestBean<Void> delete(
            @PathVariable
            @Parameter(description = "要删除的学生ID", required = true, example = "1") Integer id) {
        if (studentService.removeById(id)) {
            return RestBean.success();
        }
        return RestBean.failure(404, "学生不存在");
    }
}
