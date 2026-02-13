package com.codepilot.gateway;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提问请求体：小程序/前端 POST /api/v1/ask
 */
@Data
public class AskRequest {

    /** 用户输入：问题描述或粘贴的代码/错误 */
    @NotBlank(message = "content 不能为空")
    @Size(max = 20000)
    private String content;

    /** 可选：会话 ID，用于 7 天会话缓存 */
    private String sessionId;

    /** 可选：来源 photo/voice/paste */
    private String source;
}
