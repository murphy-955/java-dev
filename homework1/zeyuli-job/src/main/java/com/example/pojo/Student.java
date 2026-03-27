package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生信息实体类
 *
 * @author 李泽聿
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private Integer age;
    private String name;
    private Integer classId;
    
    // 关联的班级信息
    private ClassInfo classInfo;
}
