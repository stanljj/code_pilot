package com.codepilot.rag;

import java.util.List;

/**
 * RAG 检索：从 Milvus（企业知识库）取回与问题相关的片段。
 * 仅当用户属于某企业租户时使用该企业对应 collection，企业间隔离。
 */
public interface RagService {

    /**
     * 检索与 query 相关的文档片段。
     *
     * @param query    用户问题
     * @param tenantId 企业租户 ID，用于选择 collection，不可跨租户
     * @param topK     返回条数
     * @return 文档片段列表，用于拼进 LLM 上下文
     */
    List<RagChunk> retrieve(String query, String tenantId, int topK);
}
