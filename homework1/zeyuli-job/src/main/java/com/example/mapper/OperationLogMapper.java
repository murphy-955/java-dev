package com.example.mapper;

import com.example.pojo.OperationLog;

import java.util.List;

/**
 * 操作日志数据访问层接口
 *
 * @author 李泽聿
 */
public interface OperationLogMapper {
    
    /**
     * 插入操作日志
     * @param operationLog 操作日志
     * @return 影响行数
     */
    int insert(OperationLog operationLog);
    
    /**
     * 查询所有操作日志
     * @return 操作日志列表
     */
    List<OperationLog> findAll();
    
    /**
     * 根据方法名查询操作日志
     * @param methodName 方法名
     * @return 操作日志列表
     */
    List<OperationLog> findByMethodName(String methodName);
}
