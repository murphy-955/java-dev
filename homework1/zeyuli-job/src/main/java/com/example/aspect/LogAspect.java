package com.example.aspect;

import com.example.annotation.OperationLog;
import com.example.enums.OperationEnum;
import com.example.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志AOP切面
 * 基于注解 @OperationLog 实现操作日志记录
 * 支持控制台输出和数据库持久化
 *
 * @author 李泽聿
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    
    private final OperationLogMapper operationLogMapper;
    
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    
    /**
     * 定义切点：所有带有 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.example.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    /**
     * 后置通知：在标注了 @OperationLog 的方法成功执行后记录日志
     */
    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取注解信息
            OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
            
            // 执行日志记录
            recordLog(joinPoint, method, operationLogAnnotation, result);
            
        } catch (Exception e) {
            // 日志记录失败不影响主业务流程
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 记录日志（控制台 + 数据库）
     */
    private void recordLog(JoinPoint joinPoint, Method method, 
                          OperationLog annotation, Object result) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(FORMATTER);
        
        // 获取方法信息
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;
        
        // 获取操作类型和描述
        OperationEnum operationEnum = annotation.value();
        String operationDesc = buildOperationDesc(annotation, fullMethodName, 
                                                  joinPoint.getArgs(), result);
        
        // 控制台输出日志
        printConsoleLog(formattedTime, operationEnum, fullMethodName, operationDesc);
        
        // 保存到数据库
        saveToDatabase(now, operationEnum, fullMethodName, operationDesc);
    }
    
    /**
     * 构建操作描述
     */
    private String buildOperationDesc(OperationLog annotation, String methodName, 
                                      Object[] args, Object result) {
        StringBuilder desc = new StringBuilder();
        
        // 优先使用注解中自定义的描述
        if (!annotation.desc().isEmpty()) {
            desc.append(annotation.desc());
        } else {
            desc.append(annotation.value().getDescription()).append(" - ").append(methodName);
        }
        
        // 添加请求参数
        if (annotation.saveParams() && args != null && args.length > 0) {
            desc.append(" | 参数: ");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    desc.append(", ");
                }
                // 避免记录敏感信息（如密码）
                String argStr = maskSensitiveInfo(args[i]);
                desc.append(argStr);
            }
        }
        
        // 添加响应结果
        if (annotation.saveResult() && result != null) {
            desc.append(" | 结果: ").append(result);
        }
        
        return desc.toString();
    }
    
    /**
     * 脱敏处理：隐藏敏感信息
     */
    private String maskSensitiveInfo(Object arg) {
        if (arg == null) {
            return "null";
        }
        
        String argStr = arg.toString();
        
        // 简单的密码脱敏（可根据需要扩展）
        if (argStr.toLowerCase().contains("password") || 
            argStr.toLowerCase().contains("pwd")) {
            return "***";
        }
        
        // 限制长度，避免日志过长
        if (argStr.length() > 200) {
            return argStr.substring(0, 200) + "...";
        }
        
        return argStr;
    }
    
    /**
     * 控制台输出日志
     */
    private void printConsoleLog(String time, OperationEnum operationEnum,
                                  String methodName, String desc) {
        log.info("[{}] [{}] {} - {}", time, operationEnum.getName(), methodName, desc);
    }
    
    /**
     * 保存日志到数据库
     */
    private void saveToDatabase(LocalDateTime operationTime, OperationEnum operationEnum,
                                String methodName, String operationDesc) {
        // 获取请求信息
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        String ipAddress = "unknown";
        String userAgent = "unknown";
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = getClientIpAddress(request);
            userAgent = request.getHeader("User-Agent");
        }
        
        // 构建日志实体（使用全限定名避免与注解同名冲突）
        com.example.pojo.OperationLog logEntity = new com.example.pojo.OperationLog();
        logEntity.setMethodName("[" + operationEnum.getName() + "] " + methodName);
        logEntity.setOperationTime(operationTime);
        logEntity.setOperationDesc(operationDesc);
        logEntity.setIpAddress(ipAddress);
        logEntity.setUserAgent(userAgent);
        
        // 保存到数据库（失败不影响主业务）
        try {
            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志到数据库失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
