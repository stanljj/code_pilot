package com.codepilot.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Milvus 的 RAG 检索。
 * 生产需接入真实 Milvus：建表、向量化 query、检索。
 * 此处为占位实现，返回空或模拟数据，避免强依赖 Milvus 启动。
 */
@Service
public class MilvusRagService implements RagService {

    private static final Logger log = LoggerFactory.getLogger(MilvusRagService.class);

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Value("${milvus.collection-prefix:codepilot_}")
    private String collectionPrefix;

    @Override
    public List<RagChunk> retrieve(String query, String tenantId, int topK) {
        if (tenantId == null || tenantId.isBlank()) {
            return List.of();
        }
        // 占位：真实实现需
        // 1) 用 DashScope 或本地模型将 query 转为向量
        // 2) 连接 Milvus，查询 collection = collectionPrefix + tenantId
        // 3) 将 entity 转为 RagChunk（content, source, docTitle, link）
        try {
            // MilvusService.search(collectionPrefix + tenantId, queryVector, topK);
            log.debug("RAG retrieve placeholder: tenantId={}, topK={}", tenantId, topK);
            List<RagChunk> mock = new ArrayList<>();
            mock.add(new RagChunk(
                    "（示例）用户服务规范：禁止在未做 null 检查的情况下调用 user.getName()。",
                    "Confluence",
                    "用户服务规范 v2.1",
                    null
            ));
            return mock;
        } catch (Exception e) {
            log.warn("RAG 检索异常，返回空", e);
            return List.of();
        }
    }
}
