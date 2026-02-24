package com.codepilot.auth;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 微信小程序登录：POST /api/auth/login，请求体 { "code": "微信 code" }，返回 { "token": "JWT" }。
 * 小程序后续请求需在 Header 中携带：Authorization: Bearer &lt;token&gt;
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 小程序登录：用 wx.login() 获得的 code 换 JWT。
     * 后端会向微信服务器验证 code 并获取 openid，查找/创建用户、关联 tenantId，再签发 JWT。
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getCode());
        if (token == null) {
            return ResponseEntity.status(401).body(new ErrorBody("登录失败，请检查 code 或后端微信配置"));
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorBody {
        private String message;
    }
}
