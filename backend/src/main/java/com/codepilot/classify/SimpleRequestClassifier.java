package com.codepilot.classify;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 简单分类策略：有关键词或属于企业租户且命中企业关键词则走 ENTERPRISE，否则 PUBLIC。
 * 生产可改为调用小模型或规则引擎。
 */
@Component
public class SimpleRequestClassifier implements RequestClassifier {

    /** 企业上下文关键词：项目名、文档名、内部规范等 */
    private static final Pattern ENTERPRISE_HINT = Pattern.compile(
            "(?i)(规范|v\\d+\\.\\d+|jira|confluence|内部|项目|repo|仓库|pr|mr)\\b"
    );

    @Override
    public RequestType classify(String content, String tenantId) {
        if (content == null || content.isBlank()) {
            return RequestType.PUBLIC;
        }
        boolean hasTenant = tenantId != null && !tenantId.isBlank();
        boolean hasEnterpriseHint = ENTERPRISE_HINT.matcher(content).find();
        if (hasTenant && hasEnterpriseHint) {
            return RequestType.ENTERPRISE;
        }
        return RequestType.PUBLIC;
    }
}
