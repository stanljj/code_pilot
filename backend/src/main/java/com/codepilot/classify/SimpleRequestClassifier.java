package com.codepilot.classify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 请求分类：规则引擎 + 可选小模型。
 * 规则引擎：可配置企业上下文关键词正则，命中且存在租户则判 ENTERPRISE。
 * 小模型：可选调用小模型做 PUBLIC/ENTERPRISE 二分类（配置 classifier.mode 与 classifier.model）。
 */
@Component
public class SimpleRequestClassifier implements RequestClassifier {

    private static final Logger log = LoggerFactory.getLogger(SimpleRequestClassifier.class);

    /** 默认企业倾向关键词（当配置为空时使用） */
    private static final String DEFAULT_ENTERPRISE_HINT =
            "(?i)(规范|v\\d+\\.\\d+|jira|confluence|内部|项目|repo|仓库|pr|mr)\\b";

    private final ClassifierConfig config;
    private final ClassifierModelClient classifierModelClient;

    private List<Pattern> enterprisePatterns = new ArrayList<>();

    public SimpleRequestClassifier(ClassifierConfig config, ClassifierModelClient classifierModelClient) {
        this.config = config;
        this.classifierModelClient = classifierModelClient;
    }

    @PostConstruct
    public void init() {
        List<String> raw = config.getRule().getEnterprisePatterns();
        if (raw == null || raw.isEmpty()) {
            enterprisePatterns.add(Pattern.compile(DEFAULT_ENTERPRISE_HINT));
        } else {
            for (String s : raw) {
                try {
                    enterprisePatterns.add(Pattern.compile(s));
                } catch (Exception e) {
                    log.warn("无效的分类正则，已忽略: {}", s, e);
                }
            }
        }
        log.info("请求分类已加载: mode={}, 规则数={}, 小模型={}", config.getMode(), enterprisePatterns.size(), config.getModel().isEnabled());
    }

    @Override
    public RequestType classify(String content, String tenantId) {
        if (content == null || content.isBlank()) {
            return RequestType.PUBLIC;
        }
        String mode = config.getMode();
        if (mode == null) mode = "rule";

        switch (mode.toLowerCase()) {
            case "model":
                return classifierModelClient.classifyByModel(content, tenantId);
            case "rule_then_model":
                RequestType byRule = classifyByRule(content, tenantId);
                if (byRule == RequestType.ENTERPRISE) {
                    return RequestType.ENTERPRISE;
                }
                if (tenantId != null && !tenantId.isBlank() && config.getModel().isEnabled()) {
                    return classifierModelClient.classifyByModel(content, tenantId);
                }
                return RequestType.PUBLIC;
            case "rule":
            default:
                return classifyByRule(content, tenantId);
        }
    }

    /**
     * 仅用规则引擎分类：有租户且命中任一企业倾向正则则 ENTERPRISE，否则 PUBLIC。
     */
    private RequestType classifyByRule(String content, String tenantId) {
        boolean hasTenant = tenantId != null && !tenantId.isBlank();
        boolean hasEnterpriseHint = false;
        for (Pattern p : enterprisePatterns) {
            if (p.matcher(content).find()) {
                hasEnterpriseHint = true;
                break;
            }
        }
        if (hasTenant && hasEnterpriseHint) {
            return RequestType.ENTERPRISE;
        }
        return RequestType.PUBLIC;
    }
}
