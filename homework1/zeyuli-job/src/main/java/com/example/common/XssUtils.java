package com.example.common;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 * 用于对用户输入进行过滤和转义
 *
 * @author 李泽聿
 */
public class XssUtils {
    
    /**
     * 危险的 HTML 标签和事件处理器模式
     */
    private static final Pattern[] XSS_PATTERNS = new Pattern[]{
            // script 标签
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // javascript: 伪协议
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // onerror, onclick 等事件处理器
            Pattern.compile("on\\w+\\s*=\\s*['\"]?[^'\"\\s>]*", Pattern.CASE_INSENSITIVE),
            // <iframe> 标签
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // <object> 标签
            Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            // <embed> 标签
            Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
            // expression() CSS 表达式（IE）
            Pattern.compile("expression\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE),
            // eval() 函数
            Pattern.compile("eval\\s*\\(.*\\)", Pattern.CASE_INSENSITIVE),
    };
    
    /**
     * 需要转义的特殊字符
     */
    private static final char[] SPECIAL_CHARS = new char[]{'<', '>', '"', '\'', '&'};
    
    /**
     * 过滤 XSS 危险内容
     * 移除危险的 HTML 标签和脚本
     *
     * @param input 原始输入
     * @return 过滤后的安全字符串
     */
    public static String filter(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        String result = input;
        for (Pattern pattern : XSS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        return result;
    }
    
    /**
     * HTML 实体编码
     * 将特殊字符转换为 HTML 实体，防止 XSS
     *
     * @param input 原始输入
     * @return 编码后的字符串
     */
    public static String encode(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#x27;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 清理并过滤字符串（用于输入处理）
     * 先 trim，然后过滤危险内容
     *
     * @param input 原始输入
     * @return 清理后的字符串
     */
    public static String sanitize(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        return filter(input.trim());
    }
}
