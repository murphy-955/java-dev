package com.example.mapper;

import com.example.pojo.Student;
import org.apache.ibatis.annotations.Param;

/**
 * 学生数据访问层接口.
 *
 * @author example
 * @date 2026/04/10
 */
public interface StudentMapper {

    /**
     * 根据ID查询学生信息（包含班级信息）.
     *
     * @param id 学生ID
     * @return 学生对象
     */
    Student getStudentById(@Param("id") Integer id);

    /**
     * 更新学生信息.
     *
     * @param student 学生对象
     * @return 影响行数
     */
    int updateStudent(Student student);
}
