package com.codepilot.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OCR服务实现（待实现）
 */
@Slf4j
@Service
public class AliyunOcrService implements OcrService {

    @Value("${aliyun.access-key-id:#{null}}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret:#{null}}")
    private String accessKeySecret;

    @Override
    public String recognizeText(MultipartFile image) {
        if (accessKeyId == null || accessKeySecret == null) {
            log.warn("阿里云OCR配置缺失，无法使用阿里云OCR服务");
            return null;
        }

        log.info("阿里云OCR服务暂未完全实现，因为缺少依赖包");
        // 实际实现需要集成阿里云OCR API
        // 这里返回null表示此服务暂时不可用，将由其他OCR服务处理
        return null;
    }
}