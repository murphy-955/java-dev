package com.example.service;

import com.example.pojo.Student;

/**
 * 学生服务层接口.
 *
 * @author example
 * @date 2026/04/10
 */
public interface StudentService {

    /**
     * 根据ID获取学生信息.
     *
     * @param id 学生ID
     * @return 学生对象
     */
    Student getStudentById(Integer id);

    /**
     * 更新学生信息.
     *
     * @param id   学生ID
     * @param name 学生姓名
     * @param age  年龄
     * @return 是否更新成功
     */
    boolean updateStudent(Integer id, String name, Integer age);
}
