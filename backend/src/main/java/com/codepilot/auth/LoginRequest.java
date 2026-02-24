package com.codepilot.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小程序登录请求：POST /api/auth/login 请求体，传微信 wx.login() 得到的 code。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "code 不能为空")
    private String code;
}
