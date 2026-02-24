package com.codepilot.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * RAG 测试工具类：用于验证 RAG 功能是否正常工作
 */
@Component
public class RagTestUtil {

    private static final Logger log = LoggerFactory.getLogger(RagTestUtil.class);

    @Autowired
    private MilvusService milvusService;

    @PostConstruct
    public void testRagFunctionality() {
        log.info("开始测试 RAG 功能...");
        
        try {
            // 测试创建租户 collection
            milvusService.createTenantCollection("tenant");
            log.info("租户 collection 创建成功");
            
            // 准备测试数据
            RagChunk chunk1 = new RagChunk(
                "用户服务规范：禁止在未做 null 检查的情况下调用 user.getName()。",
                "Confluence", 
                "用户服务规范 v2.1", 
                "https://confluence.example.com/pages/user-service-guideline"
            );
            
            RagChunk chunk2 = new RagChunk(
                "订单服务最佳实践：在高并发场景下，使用分布式锁防止重复下单。",
                "Confluence",
                "订单服务设计规范",
                "https://confluence.example.com/pages/order-service-best-practices"
            );
            
            RagChunk chunk3 = new RagChunk(
                "数据库连接池配置：HikariCP 最大连接数应设置为 CPU 核心数的 2-4 倍。",
                "Git", 
                "数据库配置指南", 
                "https://git.example.com/repo/db-config.md"
            );
            
            List<RagChunk> testChunks = Arrays.asList(chunk1, chunk2, chunk3);
            
            // 测试插入文档
            milvusService.insertChunks("tenant", testChunks);
            log.info("测试文档插入成功");
            
            // 测试检索功能
            List<RagChunk> results = milvusService.retrieve("如何避免空指针异常", "tenant", 3);
            log.info("RAG 检索测试完成，找到 {} 个结果", results.size());
            
            for (int i = 0; i < results.size(); i++) {
                RagChunk result = results.get(i);
                log.info("结果 {}: 内容={}, 来源={}, 标题={}", i + 1, result.getContent(), result.getSource(), result.getDocTitle());
            }
            
            log.info("RAG 功能测试完成");
        } catch (Exception e) {
            log.error("RAG 功能测试失败", e);
        }
    }
}