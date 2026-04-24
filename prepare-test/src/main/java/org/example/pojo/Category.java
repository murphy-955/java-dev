package org.example.pojo;

import lombok.Data;

@Data
public class Category {
    /**
     * 分类id
     */
    private Integer id;

    /**
     * 分类名称
     */
    private String name;
}