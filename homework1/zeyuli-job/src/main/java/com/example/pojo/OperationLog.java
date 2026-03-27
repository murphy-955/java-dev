package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * @author 李泽聿
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String methodName;
    private LocalDateTime operationTime;
    private String operationDesc;
    private String ipAddress;
    private String userAgent;
}
