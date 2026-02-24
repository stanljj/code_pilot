package com.codepilot.rag;

import java.util.List;

/**
 * RAG 同步服务：负责从 Git/Jira/Confluence 等外部系统同步数据到 Milvus 向量库。
 * 提供不同数据源的同步方法，支持增量同步和全量同步。
 */
public interface RagSyncService {

    /**
     * 从 Git 仓库同步代码和文档
     *
     * @param tenantId 企业租户 ID
     * @param repoUrl  Git 仓库地址
     * @param branch   分支名称，默认为主分支
     */
    void syncFromGit(String tenantId, String repoUrl, String branch);

    /**
     * 从 Confluence 同步文档
     *
     * @param tenantId      企业租户 ID
     * @param confluenceUrl Confluence 地址
     * @param spaceKey      Space 键值
     */
    void syncFromConfluence(String tenantId, String confluenceUrl, String spaceKey);

    /**
     * 从 Jira 同步问题和评论
     *
     * @param tenantId 企业租户 ID
     * @param jiraUrl  Jira 地址
     * @param projectKey 项目键值
     */
    void syncFromJira(String tenantId, String jiraUrl, String projectKey);

    /**
     * 手动添加文档片段
     *
     * @param tenantId 企业租户 ID
     * @param chunks   文档片段列表
     */
    void addDocuments(String tenantId, List<RagChunk> chunks);

    /**
     * 删除指定租户的所有文档
     *
     * @param tenantId 企业租户 ID
     */
    void clearTenantData(String tenantId);
}