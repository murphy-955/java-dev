package com.example.enums;

import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author 李泽聿
 */
@Getter
public enum OperationEnum {
    
    /**
     * 查询操作
     */
    QUERY("查询", "查询操作"),
    
    /**
     * 新增操作
     */
    CREATE("新增", "新增操作"),
    
    /**
     * 更新操作
     */
    UPDATE("更新", "更新操作"),
    
    /**
     * 删除操作
     */
    DELETE("删除", "删除操作"),
    
    /**
     * 登录操作
     */
    LOGIN("登录", "用户登录"),
    
    /**
     * 登出操作
     */
    LOGOUT("登出", "用户登出"),
    
    /**
     * 导出操作
     */
    EXPORT("导出", "数据导出"),
    
    /**
     * 导入操作
     */
    IMPORT("导入", "数据导入"),
    
    /**
     * 其他操作
     */
    OTHER("其他", "其他操作");
    
    private final String name;
    private final String description;
    
    OperationEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
