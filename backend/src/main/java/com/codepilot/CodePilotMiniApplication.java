package com.codepilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CodePilot Mini 后端入口。
 * 负责：网关、请求分类、LLM/RAG、结果融合、安全过滤、审计。
 */
@SpringBootApplication(scanBasePackages = "com.codepilot")
public class CodePilotMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodePilotMiniApplication.class, args);
    }
}
