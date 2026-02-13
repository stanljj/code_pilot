package com.codepilot.llm;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 阿里云 Qwen-Max / CodeQwen 调用实现。
 * 使用 DashScope SDK，API Key 来自配置（环境变量）。
 */
@Service
public class QwenLlmService implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(QwenLlmService.class);

    private static final Pattern CODE_BLOCK = Pattern.compile("```[\\w]*\\n([\\s\\S]*?)```");

    @Value("${llm.qwen-max.model:qwen-max}")
    private String qwenMaxModel;

    @Value("${llm.code-qwen.model:codeqwen-codellama-7b-instruct}")
    private String codeQwenModel;

    @Value("${llm.qwen-max.api-key:}")
    private String apiKey;

    @Override
    public LlmResult chat(String userContent, String context) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DASHSCOPE_API_KEY 未配置，返回模拟结果");
            return mockResult(userContent);
        }
        boolean codeFocused = isCodeFocused(userContent);
        String model = codeFocused ? codeQwenModel : qwenMaxModel;
        String prompt = buildPrompt(userContent, context);
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(Message.builder().role(Role.SYSTEM.getValue())
                    .content("你是一个面向开发者的代码助手。请用简洁的语言给出：1) 问题分析 2) 建议 3) 如有代码请用 markdown 代码块给出。")
                    .build());
            messages.add(Message.builder().role(Role.USER.getValue()).content(prompt).build());

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(messages)
                    .build();
            GenerationResult result = new Generation().call(param);

//            if (result == null || !result.getStatus().getCode().equals("200")) {
//                log.error("LLM 调用失败: {}", result != null ? result.getStatus() : "null");
//                return mockResult(userContent);
//            }
            // 检查 result 和 output 是否为空
            if (result == null || result.getOutput() == null) {
                log.error("LLM 调用失败: result 或 output 为空");
                return mockResult(userContent);
            }

            // 检查 choices 是否为空
            List<GenerationOutput.Choice> choices = result.getOutput().getChoices();
            if (choices == null || choices.isEmpty()) {
                log.warn("LLM 返回的 choices 为空，使用默认结果");
                return mockResult(userContent);
            }
            String text = result.getOutput().getChoices().get(0).getMessage().getContent();
            return parseResponse(text, codeFocused);
        } catch (Exception e) {
            log.error("LLM 调用异常", e);
            return mockResult(userContent);
        }
    }

    private boolean isCodeFocused(String content) {
        String lower = content.toLowerCase();
        return lower.contains("代码") || lower.contains("code") || lower.contains("nullpointer")
                || lower.contains("exception") || lower.contains("react") || lower.contains("redis")
                || lower.contains("装饰器") || lower.contains("```");
    }

    private String buildPrompt(String userContent, String context) {
        if (context != null && !context.isBlank()) {
            return "参考以下企业文档片段：\n" + context + "\n\n用户问题：\n" + userContent;
        }
        return userContent;
    }

    private LlmResult parseResponse(String text, boolean isCodeFocused) {
        String analysis = "";
        String suggestion = "";
        String code = null;
        Matcher codeMatcher = CODE_BLOCK.matcher(text);
        if (codeMatcher.find()) {
            code = codeMatcher.group(1).trim();
        }
        String[] parts = text.split("```");
        if (parts.length >= 1) {
            String beforeCode = parts[0].trim();
            int firstLine = beforeCode.indexOf('\n');
            if (firstLine > 0) {
                analysis = beforeCode.substring(0, firstLine).replaceAll("^#?\\s*", "");
                suggestion = beforeCode.substring(firstLine).trim();
            } else {
                analysis = beforeCode;
            }
        }
        if (suggestion.length() > 500) {
            suggestion = suggestion.substring(0, 500) + "...";
        }
        return LlmResult.builder()
                .analysis(analysis.isEmpty() ? "已生成回复" : analysis)
                .suggestion(suggestion)
                .code(code)
                .rawResponse(text)
                .isCodeFocused(isCodeFocused)
                .build();
    }

    private LlmResult mockResult(String userContent) {
        return LlmResult.builder()
                .analysis("（演示）问题已接收，请配置 DASHSCOPE_API_KEY 后使用真实模型。")
                .suggestion("请在后端配置阿里云 DashScope API Key。")
                .code(null)
                .rawResponse("")
                .isCodeFocused(false)
                .build();
    }
}
