package com.example.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 全局安全过滤器
 * 添加 XSS 防护相关的 HTTP 响应头
 *
 * @author 李泽聿
 */
@Component
public class SecurityConfig implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 添加 X-Content-Type-Options 头部，防止 MIME 嗅探攻击
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 添加 X-Frame-Options 头部，防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // 添加 X-XSS-Protection 头部（现代浏览器已废弃，但作为纵深防御仍有用）
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 添加 Content-Security-Policy 头部，限制资源加载
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none'");

        // 添加 Referrer-Policy 头部，控制 Referrer 信息
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作
    }

    @Override
    public void destroy() {
        // 销毁操作
    }
}
