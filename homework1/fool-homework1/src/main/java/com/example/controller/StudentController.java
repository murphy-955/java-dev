package com.example.controller;

import com.example.common.Result;
import com.example.pojo.Student;
import com.example.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 * 提供RESTful风格的API接口
 *
 * @author 李泽聿
 */
@Tag(name = "学生管理", description = "学生信息的增删改查操作")
@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 根据ID查询学生信息
     * GET /student/{id}
     */
    @Operation(
            summary = "根据ID查询学生信息",
            description = "查询指定ID的学生详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Student.class))),
                    @ApiResponse(responseCode = "404", description = "学生不存在")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Result<Student>> getStudentById(
            @PathVariable @Parameter(description = "学生ID", required = true) Integer id) {
        Result<Student> result = studentService.findStudentById(id);
        return ResponseEntity.status(result.getCode())
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    /**
     * 查询所有学生信息
     * GET /student
     */
    @Operation(
            summary = "查询所有学生信息",
            description = "获取所有学生的列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping
    public ResponseEntity<Result<List<Student>>> getAllStudents() {
        Result<List<Student>> result = studentService.findAllStudents();
        return ResponseEntity.ok()
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    /**
     * 添加学生
     * POST /student
     */
    @Operation(
            summary = "添加学生",
            description = "添加新学生信息",
            responses = {
                    @ApiResponse(responseCode = "201", description = "添加成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数错误")
            }
    )
    @PostMapping
    public ResponseEntity<Result<Integer>> addStudent(@RequestBody Student student) {
        Result<Integer> result = studentService.addStudent(student);
        HttpStatus status = result.getCode() == 201 ? HttpStatus.CREATED : HttpStatus.valueOf(result.getCode());
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    /**
     * 更新学生信息
     * PUT /student/{id}
     */
    @Operation(
            summary = "更新学生信息",
            description = "更新指定ID的学生姓名和年龄",
            responses = {
                    @ApiResponse(responseCode = "200", description = "更新成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数错误"),
                    @ApiResponse(responseCode = "404", description = "学生不存在")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateStudent(
            @PathVariable @Parameter(description = "学生ID", required = true) Integer id,
            @Parameter(description = "学生姓名", required = true)
            @RequestParam("name") String name,
            @Parameter(description = "学生年龄", required = true)
            @RequestParam("age") Integer age) {
        Result<Void> result = studentService.updateStudent(id, name, age);
        return ResponseEntity.status(result.getCode())
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    /**
     * 删除学生
     * DELETE /student/{id}
     */
    @Operation(
            summary = "删除学生",
            description = "删除指定ID的学生",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功"),
                    @ApiResponse(responseCode = "404", description = "学生不存在")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteStudent(
            @PathVariable @Parameter(description = "学生ID", required = true) Integer id) {
        Result<Void> result = studentService.deleteStudent(id);
        return ResponseEntity.status(result.getCode())
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }
}
