package com.codepilot.rag.impl;

import com.codepilot.rag.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG服务实现类
 * 直接使用MilvusService进行检索操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {
    private final MilvusService milvusService;

    @Override
    public List<RagChunk> retrieve(String query, String tenantId, int topK) {
        return milvusService.retrieve(query, tenantId, topK);
    }
}