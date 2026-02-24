package com.codepilot.rag;

import io.milvus.param.R;
import io.milvus.response.SearchResultsWrapper;

import java.util.List;
import java.util.Map;

public interface MilvusService {
    /**
     * 初始化Milvus客户端
     */
    void initialize();

    /**
     * 关闭Milvus客户端
     */
    void close();

    /**
     * 创建Collection
     *
     * @param collectionName Collection名称
     * @param dimension      向量维度
     * @return 是否创建成功
     */
    boolean createCollection(String collectionName, int dimension);

    /**
     * 检查Collection是否存在
     *
     * @param collectionName Collection名称
     * @return 是否存在
     */
    boolean hasCollection(String collectionName);

    /**
     * 删除Collection
     *
     * @param collectionName Collection名称
     * @return 是否删除成功
     */
    boolean dropCollection(String collectionName);

    /**
     * 插入向量数据
     *
     * @param collectionName Collection名称
     * @param vectors        向量数据列表
     * @param metadata       元数据
     * @return 插入结果
     */
    R insert(String collectionName, List<Float[]> vectors, Map<String, Object> metadata);

    /**
     * 搜索相似向量
     *
     * @param collectionName Collection名称
     * @param queryVectors   查询向量
     * @param topK           返回前K个结果
     * @param outputFields   输出字段
     * @return 搜索结果
     */
    SearchResultsWrapper search(String collectionName, List<Float[]> queryVectors, int topK, List<String> outputFields);

//    /**
//     * 获取Collection中的向量总数
//     *
//     * @param collectionName Collection名称
//     * @return 向量总数
//     */
//    long countEntities(String collectionName);

    /**
     * 创建租户专用的Collection
     *
     * @param tenantId 租户ID
     * @return Collection名称
     */
    String createTenantCollection(String tenantId);

    /**
     * 向租户Collection插入文档片段
     *
     * @param tenantId 租户ID
     * @param chunks   文档片段列表
     */
    void insertChunks(String tenantId, List<RagChunk> chunks);

    /**
     * 从租户Collection检索相关文档
     *
     * @param query    查询文本
     * @param tenantId 租户ID
     * @param topK     返回结果数量
     * @return 相关文档片段
     */
    List<RagChunk> retrieve(String query, String tenantId, int topK);
}