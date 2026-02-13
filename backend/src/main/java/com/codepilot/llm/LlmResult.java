package com.codepilot.llm;

import lombok.Builder;
import lombok.Data;

/**
 * 大模型单次调用结果（未融合、未脱敏）。
 */
@Data
@Builder
public class LlmResult {

    private String analysis;
    private String suggestion;
    private String code;
    private String rawResponse;
    private boolean isCodeFocused;
}
