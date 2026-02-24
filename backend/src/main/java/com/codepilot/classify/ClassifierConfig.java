package com.codepilot.classify;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求分类配置：规则引擎的匹配模式、是否启用小模型、小模型名称等。
 */
@Component
@ConfigurationProperties(prefix = "classifier")
public class ClassifierConfig {

    /** rule=仅规则；model=仅小模型；rule_then_model=先规则后小模型 */
    private String mode = "rule_then_model";

    private Rule rule = new Rule();
    private Model model = new Model();

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode != null ? mode : "rule";
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule != null ? rule : new Rule();
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model != null ? model : new Model();
    }

    public static class Rule {
        /** 企业上下文相关正则，命中任一则结合 tenantId 可判 ENTERPRISE */
        private List<String> enterprisePatterns = new ArrayList<>();

        public List<String> getEnterprisePatterns() {
            return enterprisePatterns;
        }

        public void setEnterprisePatterns(List<String> enterprisePatterns) {
            this.enterprisePatterns = enterprisePatterns != null ? enterprisePatterns : new ArrayList<>();
        }
    }

    public static class Model {
        private boolean enabled = true;
        private String model = "qwen-turbo";
        private String apiKey = "sk-36a9a876df164f20aeab8aeae4c7870c";
        private int maxTokens = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model != null ? model : "qwen-turbo";
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey != null ? apiKey : "";
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = Math.max(1, maxTokens);
        }
    }
}
