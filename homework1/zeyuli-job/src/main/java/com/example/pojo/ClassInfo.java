package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 班级信息实体类
 *
 * @author 李泽聿
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String className;
}
