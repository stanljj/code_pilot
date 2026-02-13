package com.codepilot.llm;

/**
 * 大模型调用：Qwen-Max（通用）/ CodeQwen（代码专用）。
 * 密钥从配置/环境变量读取，禁止硬编码。
 */
public interface LlmService {

    /**
     * 根据是否代码类问题选择模型，返回结构化结果。
     *
     * @param userContent 用户输入
     * @param context     RAG 检索到的上下文（可为 null）
     * @return 分析、建议、代码片段等
     */
    LlmResult chat(String userContent, String context);
}
