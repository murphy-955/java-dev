package com.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 权限验证拦截器.
 * 验证请求头中的Authorization Token.
 *
 * @author example
 * @date 2026/04/10
 */
public class AutoInterceptor implements HandlerInterceptor {

    /**
     * 日志记录器.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoInterceptor.class);

    /**
     * 有效的Token值.
     */
    private static final String VALID_TOKEN = "valid-token-123456";

    /**
     * 请求头名称.
     */
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader(AUTH_HEADER);
        LOGGER.debug("请求路径：{}，Token：{}", request.getRequestURI(), token);

        if (isValidToken(token)) {
            return true;
        }

        // Token无效，返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":401,\"message\":\"未授权，请提供有效的Token\"}");
        writer.flush();
        return false;
    }

    /**
     * 验证Token是否有效.
     *
     * @param token 请求头中的Token
     * @return 是否有效
     */
    private boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        // 支持Bearer格式或直接Token
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return VALID_TOKEN.equals(token.trim());
    }
}
