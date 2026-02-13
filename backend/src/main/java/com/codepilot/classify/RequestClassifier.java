package com.codepilot.classify;

/**
 * 请求分类器：根据内容与用户租户判断走公有大模型还是 RAG。
 */
public interface RequestClassifier {

    /**
     * 判断请求类型。
     *
     * @param content   用户输入
     * @param tenantId  企业租户 ID，null 表示未绑定企业
     * @return PUBLIC 或 ENTERPRISE
     */
    RequestType classify(String content, String tenantId);
}
