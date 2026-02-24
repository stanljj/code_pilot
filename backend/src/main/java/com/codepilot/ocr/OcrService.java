package com.codepilot.ocr;

import org.springframework.web.multipart.MultipartFile;

/**
 * OCR服务接口：处理图片文字识别
 */
public interface OcrService {
    /**
     * 识别图片中的文字内容
     * @param image 图片文件
     * @return 识别出的文字内容
     */
    String recognizeText(MultipartFile image);
}