package com.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志AOP切面类.
 * 每次操作业务方法后在控制台打印时间戳 + 操作方法名.
 *
 * @author example
 * @date 2026/04/10
 */
@Aspect
@Component
public class LogAspect {

    /**
     * 日志记录器.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 日期时间格式化器.
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    /**
     * 定义切点：拦截Service层所有方法.
     */
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void servicePointcut() {
    }

    /**
     * 后置通知：方法成功执行后记录日志.
     *
     * @param joinPoint 连接点
     */
    @AfterReturning("servicePointcut()")
    public void doAfterReturning(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String currentTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LOGGER.info("{} {}", currentTime, methodName);
    }
}
