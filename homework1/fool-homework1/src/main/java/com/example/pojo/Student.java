package com.example.pojo;

import java.io.Serializable;

/**
 * 学生信息实体类
 *
 * @author 李泽聿
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer age;
    private String name;
    private Integer classId;

    // 关联的班级信息
    private ClassInfo classInfo;

    public Student() {
    }

    public Student(Integer id, Integer age, String name, Integer classId, ClassInfo classInfo) {
        this.id = id;
        this.age = age;
        this.name = name;
        this.classId = classId;
        this.classInfo = classInfo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", classId=" + classId +
                ", classInfo=" + classInfo +
                '}';
    }
}
