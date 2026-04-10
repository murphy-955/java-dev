package com.example.pojo;

import java.io.Serializable;

/**
 * 学生实体类.
 *
 * @author example
 * @date 2026/04/10
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学生ID.
     */
    private Integer id;

    /**
     * 学生姓名.
     */
    private String name;

    /**
     * 年龄.
     */
    private Integer age;

    /**
     * 班级ID.
     */
    private Integer cId;

    /**
     * 班级信息（一对多双向关联）.
     */
    private ClassInfo classInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getCId() {
        return cId;
    }

    public void setCId(Integer cId) {
        this.cId = cId;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    @Override
    public String toString() {
        return "Student{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", age=" + age
            + ", cId=" + cId
            + '}';
    }
}
