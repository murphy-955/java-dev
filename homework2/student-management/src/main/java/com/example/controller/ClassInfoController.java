package com.example.controller;

import com.example.pojo.ClassInfo;
import com.example.service.ClassInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 班级控制器类.
 *
 * @author example
 * @date 2026/04/10
 */
@RestController
@RequestMapping("/classInfo")
public class ClassInfoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfoController.class);

    private final ClassInfoService classInfoService;

    @Autowired
    public ClassInfoController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    /**
     * 根据ID查询班级信息.
     *
     * @param id 班级ID
     * @return 班级信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getClassInfoById(@PathVariable Integer id) {
        LOGGER.info("查询班级信息，id={}", id);
        ClassInfo classInfo = classInfoService.getClassInfoById(id);
        Map<String, Object> result = new HashMap<>(4);
        if (Objects.isNull(classInfo)) {
            result.put("code", 404);
            result.put("message", "班级不存在");
            return ResponseEntity.status(404).body(result);
        }
        result.put("code", 200);
        result.put("data", classInfo);
        return ResponseEntity.ok(result);
    }
}
