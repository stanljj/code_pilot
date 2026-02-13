package com.codepilot.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 企业微信 OAuth 2.0：登录跳转与回调换 token。
 * 实际 token 存储与校验需接入 Spring Security 或 JWT。
 */
@RestController
@RequestMapping("/api/auth/wecom")
public class WeComAuthController {

    @Value("${wecom.corp-id:}")
    private String corpId;

    @Value("${wecom.redirect-uri:}")
    private String redirectUri;

    @Value("${wecom.agent-id:}")
    private String agentId;

    /**
     * 前端/小程序跳转：重定向到企微授权页。
     */
    @GetMapping("/login")
    public ResponseEntity<String> login() {
        if (corpId == null || corpId.isBlank()) {
            return ResponseEntity.badRequest().body("WECOM_CORP_ID 未配置");
        }
        String state = "codepilot";
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + corpId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_base"
                + "&state=" + state;
        return ResponseEntity.ok().body("{\"loginUrl\":\"" + url + "\"}");
    }

    /**
     * 企微回调：用 code 换 userid，再换自己的 token（此处仅示意）。
     */
    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("缺少 code");
        }
        // TODO: 调用企微 API 用 code + corp_id + secret 换 userid，再生成 JWT 并重定向到前端
        return ResponseEntity.ok().body("{\"message\":\"请使用 code 换取 userid 并签发 token\"}");
    }
}
