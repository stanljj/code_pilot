package com.codepilot.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagInitializationService {

    private final MilvusService milvusService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRagService() {
        log.info("开始初始化RAG服务...");
        try {
            milvusService.initialize();
            log.info("RAG服务初始化完成");
        } catch (Exception e) {
            log.error("RAG服务初始化失败", e);
        }
    }
}