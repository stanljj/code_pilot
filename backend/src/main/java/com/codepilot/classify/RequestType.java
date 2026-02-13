package com.codepilot.classify;

/**
 * 请求类型：公共问题直连大模型；含企业上下文走 RAG。
 */
public enum RequestType {
    /** 公共问题，不涉及企业私有知识 */
    PUBLIC,
    /** 含企业上下文，需走 RAG（Git/Jira/Confluence 等） */
    ENTERPRISE
}
