package com.codepilot.rag;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 向量嵌入服务：将文本转换为向量表示，用于 RAG 检索。
 */
@Service
@Slf4j
public class EmbeddingService {

    @Value("${llm.embedding.api-key:#{null}}")
    private String apiKey;

    @Value("${llm.embedding.model:text-embedding-v2}")
    private String embeddingModel;

    /**
     * 将文本转换为向量
     *
     * @param text 输入文本
     * @return 向量表示
     */
    public double[] embedText(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DASHSCOPE_API_KEY 未配置，使用模拟向量");
            return mockEmbedding(text);
        }

        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .apiKey(apiKey)
                    .model(embeddingModel)
                    .text(text)
                    .build();

            TextEmbedding embedding = new TextEmbedding();
            TextEmbeddingResult result = embedding.call(param);

            if (result == null || result.getOutput() == null || 
                result.getOutput().getEmbeddings() == null || 
                result.getOutput().getEmbeddings().isEmpty()) {
                log.warn("向量嵌入结果为空，使用模拟向量");
                return mockEmbedding(text);
            }

            // 获取第一个向量（因为我们只传了一个文本）
            List<Double> vector = result.getOutput().getEmbeddings().get(0).getEmbedding();
            return vector.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (NoApiKeyException e) {
            log.error("向量嵌入 API 调用失败 - API Key 未配置: {}", e.getMessage());
            return mockEmbedding(text);
        } catch (Exception e) {
            log.error("向量嵌入 API 调用失败: {}", e.getMessage(), e);
            return mockEmbedding(text);
        }
    }

    /**
     * 模拟向量生成（用于演示和测试）
     */
    private double[] mockEmbedding(String text) {
        // 创建一个简单的哈希向量，确保相同文本产生相同向量
        byte[] bytes = text.getBytes();
        double[] vector = new double[1536]; // 与 text-embedding-v2 维度一致
        
        for (int i = 0; i < bytes.length; i++) {
            vector[i % vector.length] += bytes[i];
        }
        
        // 归一化
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }
}