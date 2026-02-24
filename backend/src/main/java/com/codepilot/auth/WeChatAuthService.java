package com.codepilot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信小程序登录：用 code 调用微信 code2Session 接口，换取 openid 与 session_key。
 * 微信接口可能返回 Content-Type: text/plain，故先按字符串读取再手动解析 JSON。
 */
@Service
public class WeChatAuthService {

    private static final Logger log = LoggerFactory.getLogger(WeChatAuthService.class);
    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wechat.miniprogram.app-id:}")
    private String appId;

    @Value("${wechat.miniprogram.app-secret:}")
    private String appSecret;

    /**
     * 使用微信登录 code 换取 openid（及 session_key）。
     *
     * @param code 小程序 wx.login() 得到的 code
     * @return openid，失败时返回 null 并已打日志
     */
    public String getOpenIdByCode(String code) {
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.warn("微信小程序 app-id 或 app-secret 未配置，无法校验 code");
            return null;
        }
        String url = String.format(CODE2SESSION_URL, appId, appSecret, code);
        try {
            String body = restTemplate.getForObject(url, String.class);
            if (body == null || body.isBlank()) {
                log.warn("微信 code2Session 返回空 body");
                return null;
            }
            WeChatCode2SessionResponse resp = objectMapper.readValue(body, WeChatCode2SessionResponse.class);
            if (resp != null && resp.isSuccess() && resp.getOpenid() != null) {
                return resp.getOpenid();
            }
            if (resp != null) {
                log.warn("微信 code2Session 失败: errcode={}, errmsg={}", resp.getErrcode(), resp.getErrmsg());
            }
        } catch (Exception e) {
            log.error("调用微信 code2Session 异常", e);
        }
        return null;
    }
}
