package com.example.service;

import com.example.common.Result;
import com.example.pojo.Student;

import java.util.List;

/**
 * 学生服务接口
 *
 * @author 李泽聿
 */
public interface StudentService {
    
    /**
     * 根据ID查询学生信息
     * @param id 学生ID
     * @return 查询结果
     */
    Result<Student> findStudentById(Integer id);
    
    /**
     * 查询所有学生信息
     * @return 学生列表结果
     */
    Result<List<Student>> findAllStudents();
    
    /**
     * 更新学生信息
     * @param id 学生ID
     * @param name 学生姓名
     * @param age 学生年龄
     * @return 操作结果
     */
    Result<Void> updateStudent(Integer id, String name, Integer age);
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 操作结果（包含新增学生ID）
     */
    Result<Integer> addStudent(Student student);
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 操作结果
     */
    Result<Void> deleteStudent(Integer id);
}
