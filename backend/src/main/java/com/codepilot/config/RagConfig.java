package com.codepilot.config;

import com.codepilot.rag.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 相关配置
 * 统一管理Milvus和Embedding服务配置
 */
@Configuration
public class RagConfig {

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private int port;

    @Value("${milvus.collection-prefix}")
    private String collectionPrefix;

    @Value("${milvus.dimension}")
    private int dimension;

    @Value("${milvus.max-top-k}")
    private int maxTopK;

    @Value("${milvus.index-type}")
    private String indexType;

    @Value("${milvus.metric-type}")
    private String metricType;

    @Value("${milvus.connect-timeout-millis}")
    private int connectTimeoutMillis;

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getCollectionPrefix() { return collectionPrefix; }
    public int getDimension() { return dimension; }
    public int getMaxTopK() { return maxTopK; }
    public String getIndexType() { return indexType; }
    public String getMetricType() { return metricType; }
    public int getConnectTimeoutMillis() { return connectTimeoutMillis; }

    /**
     * 注册 EmbeddingService Bean
     */
    @Bean
    public EmbeddingService embeddingService() {
        return new EmbeddingService();
    }

    /**
     * 注册 MilvusService Bean
     */
    @Bean
    public MilvusService milvusService(RagConfig ragConfig, EmbeddingService embeddingService) {
        return new com.codepilot.rag.impl.MilvusServiceImpl(ragConfig, embeddingService);
    }
    
    /**
     * 注册 RagSyncService Bean
     */
    @Bean
    public RagSyncService ragSyncService() {
        return new DefaultRagSyncService();
    }
}