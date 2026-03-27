package com.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 权限验证拦截器
 * 验证请求头中的Authorization令牌
 * @author 李泽聿
 * @since 2026-03-27 15:07
 */
@Slf4j
public class AutoInterceptor implements HandlerInterceptor {
    
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 简化的token验证（实际项目中应该使用JWT等安全方式）
    private static final String VALID_TOKEN = "valid-token-123456";
    
    /**
     * 在Controller方法执行前进行拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 记录请求日志
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("[{}] 拦截到请求: {} {}", timestamp, method, uri);
        
        // 放行Swagger相关请求
        if (uri.contains("/swagger") || uri.contains("/api-docs") || 
            uri.contains("/v3/api-docs") || uri.contains("swagger-ui")) {
            log.info("[{}] Swagger文档请求，直接放行", timestamp);
            return true;
        }
        
        // 获取请求头中的Authorization
        String authorization = request.getHeader("Authorization");
        
        // 验证token（简化版，实际应该使用JWT解析）
        if (authorization == null || authorization.isEmpty()) {
            log.warn("[{}] 验证失败: 缺少Authorization请求头", timestamp);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权：缺少Authorization请求头\"}");
            return false;
        }
        
        // 简单验证（实际项目中应该解析JWT）
        // 这里支持两种格式：Bearer token 或直接 token
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        if (!VALID_TOKEN.equals(token)) {
            log.warn("[{}] 验证失败: 无效的Token", timestamp);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权：无效的Token\"}");
            return false;
        }
        
        log.info("[{}] 权限验证通过", timestamp);
        return true;
    }
    
    /**
     * 在Controller方法执行后，视图渲染前执行
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull Object handler, ModelAndView modelAndView) {
        // 可以在这里添加一些后置处理逻辑
    }
    
    /**
     * 在视图渲染完成后执行
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        // 可以在这里进行一些资源清理工作
        if (ex != null) {
            log.error("请求处理异常: {}", ex.getMessage(), ex);
        }
    }
}
