package com.codepilot.fusion;

import com.codepilot.gateway.AskResponse;
import com.codepilot.llm.LlmResult;
import com.codepilot.rag.RagChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 结果融合：将 LLM 结果与 RAG 文档引用合并为统一响应结构。
 */
@Service
public class FusionService {

    public AskResponse fuse(LlmResult llmResult, List<RagChunk> ragChunks, String requestType) {
        List<AskResponse.DocRef> docRefs = ragChunks.stream()
                .map(c -> new AskResponse.DocRef(c.getDocTitle(), c.getLink()))
                .collect(Collectors.toList());

        return AskResponse.builder()
                .requestType(requestType != null ? requestType.toLowerCase() : "public")
                .analysis(llmResult.getAnalysis())
                .suggestion(llmResult.getSuggestion())
                .code(llmResult.getCode())
                .docRefs(docRefs.isEmpty() ? null : docRefs)
                .cardUrl(null) // 分享卡片可由后续服务生成
                .build();
    }
}
