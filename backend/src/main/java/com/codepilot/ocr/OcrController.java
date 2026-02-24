package com.codepilot.ocr;

import com.codepilot.gateway.AskController;
import com.codepilot.gateway.AskRequest;
import com.codepilot.gateway.AskResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR控制器：处理图片上传并进行文字识别，然后调用ask流程
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class OcrController {

    private final OcrService ocrService;
    private final AskController askController;

    @Autowired
    public OcrController(@Qualifier("aliyunOcrService") OcrService ocrService, AskController askController) {
        this.ocrService = ocrService;
        this.askController = askController;
    }

    /**
     * OCR接口：接收图片，识别文字后调用ask流程
     */
    @PostMapping(value = "/ask/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AskResponse> ocrAsk(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "source", required = false, defaultValue = "photo") String source,
            @AuthenticationPrincipal UserDetails user) {
        
        log.info("接收到OCR请求，图片大小: {} bytes", image.getSize());
        
        // 验证上传的文件
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查文件类型是否为图片
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("上传的文件不是图片格式: {}", contentType);
            return ResponseEntity.badRequest().build();
        }
        
        // 使用OCR服务识别图片中的文字
        String recognizedText = ocrService.recognizeText(image);
        
        if (recognizedText == null || recognizedText.trim().isEmpty()) {
            log.warn("OCR识别未返回有效文本");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("OCR识别完成，识别到文本长度: {}", recognizedText.length());
        
        // 创建AskRequest对象，将识别出的文本作为内容
        AskRequest askRequest = new AskRequest();
        askRequest.setContent(recognizedText);
        askRequest.setSessionId(sessionId);
        askRequest.setSource(source);
        
        // 调用现有的ask流程
        return askController.ask(askRequest, user);
    }
}