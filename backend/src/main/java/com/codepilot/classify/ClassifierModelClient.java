package com.codepilot.classify;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 使用小模型对用户问题做 PUBLIC/ENTERPRISE 分类。
 * 仅在 classifier.model.enabled 且配置了 apiKey 时生效；未配置或调用失败时返回 PUBLIC。
 */
@Component
public class ClassifierModelClient {

    private static final Logger log = LoggerFactory.getLogger(ClassifierModelClient.class);
    private static final Pattern ENTERPRISE_PATTERN = Pattern.compile("ENTERPRISE", Pattern.CASE_INSENSITIVE);

    private final ClassifierConfig config;

    @Value("${llm.qwen-max.api-key:}")
    private String fallbackApiKey;

    public ClassifierModelClient(ClassifierConfig config) {
        this.config = config;
    }

    /**
     * 调用小模型判断是否需要企业知识库（RAG）。仅回复 PUBLIC 或 ENTERPRISE。
     *
     * @param content  用户输入
     * @param tenantId 当前租户，可为 null
     * @return PUBLIC 或 ENTERPRISE；失败或未配置时返回 PUBLIC
     */
    public RequestType classifyByModel(String content, String tenantId) {
        if (!config.getModel().isEnabled()) {
            return RequestType.PUBLIC;
        }
        String apiKey = config.getModel().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = fallbackApiKey;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("分类小模型未配置 apiKey，跳过模型分类");
            return RequestType.PUBLIC;
        }
        String model = config.getModel().getModel();
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder().role(Role.SYSTEM.getValue())
                    .content("你只做二选一分类。用户问题是否需要结合企业内部知识（如公司规范、Jira、Confluence、内部文档）来回答？只需回复一个单词：PUBLIC（不需要）或 ENTERPRISE（需要）。不要解释。")
                    .build());
            String userContent = content;
            if (tenantId != null && !tenantId.isBlank()) {
                userContent = "[租户: " + tenantId + "]\n" + content;
            }
            messages.add(Message.builder().role(Role.USER.getValue()).content(userContent).build());

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(messages)
                    .build();
            GenerationResult result = new Generation().call(param);

            if (result == null || result.getOutput() == null) {
                log.warn("分类小模型返回为空");
                return RequestType.PUBLIC;
            }
            List<GenerationOutput.Choice> choices = result.getOutput().getChoices();
            if (choices == null || choices.isEmpty()) {
                return RequestType.PUBLIC;
            }
            String text = choices.get(0).getMessage().getContent();
            if (text != null && ENTERPRISE_PATTERN.matcher(text.trim()).find()) {
                return RequestType.ENTERPRISE;
            }
            return RequestType.PUBLIC;
        } catch (Exception e) {
            log.warn("分类小模型调用异常，回退为 PUBLIC: {}", e.getMessage());
            return RequestType.PUBLIC;
        }
    }
}
