package com.codepilot.rag;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.CheckHealthResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Milvus 健康检查指示器
 * 用于监控 Milvus 连接状态
 */
@Component
public class MilvusHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(MilvusHealthIndicator.class);

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    @Override
    public Health health() {
        try {
            // 创建临时连接以测试健康状态
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(milvusHost)
                    .withPort(milvusPort)
                    .build();

            MilvusServiceClient tempClient = new MilvusServiceClient(connectParam);
            
            // 检查 Milvus 服务是否健康
            R<CheckHealthResponse> healthCheck = tempClient.checkHealth();
            if (healthCheck != null && healthCheck.getData() != null) {
                tempClient.close();
                return Health.up()
                        .withDetail("milvus_host", milvusHost)
                        .withDetail("milvus_port", milvusPort)
                        .withDetail("status", "healthy")
                        .build();
            } else {
                tempClient.close();
                return Health.down()
                        .withDetail("milvus_host", milvusHost)
                        .withDetail("milvus_port", milvusPort)
                        .withDetail("status", "unhealthy")
                        .build();
            }
        } catch (Exception e) {
            log.warn("Milvus health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("milvus_host", milvusHost)
                    .withDetail("milvus_port", milvusPort)
                    .withDetail("status", "connection_failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}