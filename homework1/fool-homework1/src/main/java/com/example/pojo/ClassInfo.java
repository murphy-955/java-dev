package com.example.pojo;

import java.io.Serializable;

/**
 * 班级信息实体类
 *
 * @author 李泽聿
 */
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String className;

    public ClassInfo() {
    }

    public ClassInfo(Integer id, String className) {
        this.id = id;
        this.className = className;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "id=" + id +
                ", className='" + className + '\'' +
                '}';
    }
}
