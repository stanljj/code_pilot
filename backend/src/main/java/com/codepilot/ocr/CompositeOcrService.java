package com.codepilot.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 复合OCR服务：依次尝试多种OCR实现
 */
@Slf4j
@Service
public class CompositeOcrService implements OcrService {

    private final List<OcrService> ocrServices;

    public CompositeOcrService(List<OcrService> ocrServices) {
        this.ocrServices = ocrServices;
    }

    @Override
    public String recognizeText(MultipartFile image) {
        log.info("开始OCR识别，共有{}个OCR服务提供者", ocrServices.size());
        
        for (OcrService service : ocrServices) {
            try {
                log.debug("尝试使用OCR服务: {}", service.getClass().getSimpleName());
                String result = service.recognizeText(image);
                
                if (result != null && !result.trim().isEmpty()) {
                    log.info("OCR识别成功，使用服务: {}", service.getClass().getSimpleName());
                    return result;
                }
            } catch (Exception e) {
                log.warn("OCR服务 {} 识别失败: {}", service.getClass().getSimpleName(), e.getMessage());
                continue;
            }
        }
        
        log.warn("所有OCR服务均未能识别出有效文本");
        return null;
    }
}