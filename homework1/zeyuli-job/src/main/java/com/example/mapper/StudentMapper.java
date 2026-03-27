package com.example.mapper;

import com.example.pojo.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生数据访问层接口
 *
 * @author 李泽聿
 */
public interface StudentMapper {
    
    /**
     * 根据ID查询学生信息
     * @param id 学生ID
     * @return 学生信息
     */
    Student findById(@Param("id") Integer id);
    
    /**
     * 查询所有学生信息
     * @return 学生列表
     */
    List<Student> findAll();
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 影响行数
     */
    int update(Student student);
    
    /**
     * 插入学生信息
     * @param student 学生信息
     * @return 影响行数
     */
    int insert(Student student);
    
    /**
     * 删除学生信息
     * @param id 学生ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);
}
