package com.example.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 班级实体类.
 *
 * @author example
 * @date 2026/04/10
 */
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级ID.
     */
    private Integer id;

    /**
     * 班级名称.
     */
    private String name;

    /**
     * 学生列表（一对多双向关联）.
     */
    private List<Student> students;

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

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "ClassInfo{"
            + "id=" + id
            + ", name='" + name + '\''
            + '}';
    }
}
