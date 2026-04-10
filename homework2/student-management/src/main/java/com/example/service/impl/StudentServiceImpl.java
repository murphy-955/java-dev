package com.example.service.impl;

import com.example.mapper.StudentMapper;
import com.example.pojo.Student;
import com.example.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 学生服务层实现类.
 *
 * @author example
 * @date 2026/04/10
 */
@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentMapper studentMapper;

    @Autowired
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.getStudentById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudent(Integer id, String name, Integer age) {
        Student student = studentMapper.getStudentById(id);
        if (Objects.isNull(student)) {
            LOGGER.warn("学生不存在，id={}", id);
            return false;
        }
        student.setName(name);
        student.setAge(age);
        int rows = studentMapper.updateStudent(student);
        return rows > 0;
    }
}
