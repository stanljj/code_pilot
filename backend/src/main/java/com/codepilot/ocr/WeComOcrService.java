package com.codepilot.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * 微信企业号OCR服务实现
 */
@Slf4j
@Service
public class WeComOcrService implements OcrService {

    @Value("${wecom.corp-id:#{null}}")
    private String corpId;

    @Value("${wecom.secret:#{null}}")
    private String secret;

    @Value("${wecom.agent-id:#{null}}")
    private String agentId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeComOcrService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String recognizeText(MultipartFile image) {
        try {
            // 获取access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取微信企业号access_token失败");
                return null;
            }

            // 调用微信OCR接口
            return callWeComOcr(image, accessToken);
        } catch (Exception e) {
            log.error("微信OCR识别失败", e);
            return null;
        }
    }

    private String getAccessToken() {
        if (corpId == null || secret == null) {
            log.warn("企业微信配置缺失，无法获取access_token");
            return null;
        }

        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken" +
                "?corpid=" + corpId +
                "&corpsecret=" + secret;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String errcode = jsonNode.get("errcode").asText();
            
            if ("0".equals(errcode)) {
                return jsonNode.get("access_token").asText();
            } else {
                log.error("获取access_token失败: {}", jsonNode.get("errmsg").asText());
                return null;
            }
        } catch (Exception e) {
            log.error("获取access_token异常", e);
            return null;
        }
    }

    private String callWeComOcr(MultipartFile image, String accessToken) {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/media/ocr/scan?access_token=" + accessToken + "&type=scan";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
            body.add("media", imageResource);
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            return null;
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String errcode = jsonNode.get("errcode").asText();
            
            if ("0".equals(errcode)) {
                // 解析识别结果
                JsonNode resultNode = jsonNode.get("result");
                if (resultNode != null && resultNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode item : resultNode) {
                        sb.append(item.get("text").asText()).append("\n");
                    }
                    return sb.toString().trim();
                }
            } else {
                log.error("OCR识别失败: {}", jsonNode.get("errmsg").asText());
            }
        } catch (Exception e) {
            log.error("调用微信OCR接口异常", e);
        }
        
        return null;
    }
}