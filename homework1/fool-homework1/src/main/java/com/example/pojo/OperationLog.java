package com.example.pojo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * @author 李泽聿
 */
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String methodName;
    private LocalDateTime operationTime;
    private String operationDesc;
    private String ipAddress;
    private String userAgent;

    public OperationLog() {
    }

    public OperationLog(Integer id, String methodName, LocalDateTime operationTime, 
                        String operationDesc, String ipAddress, String userAgent) {
        this.id = id;
        this.methodName = methodName;
        this.operationTime = operationTime;
        this.operationDesc = operationDesc;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", methodName='" + methodName + '\'' +
                ", operationTime=" + operationTime +
                ", operationDesc='" + operationDesc + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
