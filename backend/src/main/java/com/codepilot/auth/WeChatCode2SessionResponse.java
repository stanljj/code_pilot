package com.codepilot.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 code2Session 接口返回体。
 * 成功：openid, session_key, unionid(可选)
 * 失败：errcode, errmsg
 */
@Data
public class WeChatCode2SessionResponse {

    @JsonProperty("openid")
    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    @JsonProperty("unionid")
    private String unionid;

    @JsonProperty("errcode")
    private Integer errcode;

    @JsonProperty("errmsg")
    private String errmsg;

    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }
}
