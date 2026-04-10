package com.example.controller;

import com.example.pojo.Student;
import com.example.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 学生控制器类.
 *
 * @author example
 * @date 2026/04/10
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 根据ID查询学生信息.
     *
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable Integer id) {
        LOGGER.info("查询学生信息，id={}", id);
        Student student = studentService.getStudentById(id);
        Map<String, Object> result = new HashMap<>(4);
        if (Objects.isNull(student)) {
            result.put("code", 404);
            result.put("message", "学生不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 200);
        result.put("data", student);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新学生信息.
     *
     * @param id   学生ID
     * @param name 姓名
     * @param age  年龄
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam Integer age) {
        LOGGER.info("更新学生信息，id={}，name={}，age={}", id, name, age);
        boolean success = studentService.updateStudent(id, name, age);
        Map<String, Object> result = new HashMap<>(4);
        if (success) {
            result.put("code", 200);
            result.put("message", "更新成功");
            return ResponseEntity.ok(result);
        }
        result.put("code", 500);
        result.put("message", "更新失败");
        return ResponseEntity.status(500).body(result);
    }
}
