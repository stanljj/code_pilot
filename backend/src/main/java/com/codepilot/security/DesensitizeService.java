package com.codepilot.security;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 敏感信息脱敏：IP、手机号、密钥等替换为占位符。
 * 配置来自 application.yml security.desensitize.patterns，此处内置默认规则。
 */
@Service
public class DesensitizeService {

    private static final Pattern IP = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern PASSWORD_LIKE = Pattern.compile(
            "(?i)(password|apikey|secret|token)\\s*[:=]\\s*['\"]?[^'\"\\s]+",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 对文本做脱敏，返回新字符串。
     */
    public String desensitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String out = text;
        out = IP.matcher(out).replaceAll("[IP]");
        out = PHONE.matcher(out).replaceAll("[PHONE]");
        out = PASSWORD_LIKE.matcher(out).replaceAll("$1: [REDACTED]");
        return out;
    }
}
