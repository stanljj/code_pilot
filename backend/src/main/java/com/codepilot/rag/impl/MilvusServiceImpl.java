package com.codepilot.rag.impl;

import com.codepilot.config.RagConfig;
import com.codepilot.rag.*;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.*;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilvusServiceImpl implements MilvusService {

    private final RagConfig ragConfig;
    private final EmbeddingService embeddingService;
    private MilvusClient client;

    @Override
    public void initialize() {
        if (client != null) return;

        try {
            ConnectParam.Builder builder = ConnectParam.newBuilder()
                    .withHost(ragConfig.getHost())
                    .withPort(ragConfig.getPort())
                    .withConnectTimeout(ragConfig.getConnectTimeoutMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

            // 如果启用了认证（如 Zilliz Cloud 或开启 auth 的 Milvus）
            // 可在此处添加用户名密码（2.4.6 SDK 支持）
            // builder.withAuthorization(ragConfig.getUsername(), ragConfig.getPassword());

            ConnectParam connectParam = builder.build();
            this.client = new MilvusServiceClient(connectParam);

            log.info("Milvus客户端初始化成功，连接到 {}:{}", ragConfig.getHost(), ragConfig.getPort());
        } catch (Exception e) {
            log.error("Milvus客户端初始化失败", e);
            throw new RuntimeException("Milvus客户端初始化失败", e);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
            log.info("Milvus客户端已关闭");
        }
    }

    @Override
    public boolean createCollection(String collectionName, int dimension) {
        initialize();

        if (hasCollection(collectionName)) {
            log.info("Collection {} 已存在", collectionName);
            return true;
        }

        try {
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();

            FieldType vectorField = FieldType.newBuilder()
                    .withName("vector")
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();

            FieldType contentField = FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();

            FieldType metadataField = FieldType.newBuilder()
                    .withName("metadata")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();

            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("CodePilot RAG collection")
                    .addFieldType(idField)
                    .addFieldType(vectorField)
                    .addFieldType(contentField)
                    .addFieldType(metadataField)
                    .build();

            R<RpcStatus> response = client.createCollection(createCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("创建Collection失败: {}", response.getMessage());
                return false;
            }

            // 创建索引
            String extraParam = "{\"nlist\":128}";
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("vector")
                    .withIndexType(IndexType.valueOf(ragConfig.getIndexType().toUpperCase()))
                    .withMetricType(MetricType.valueOf(ragConfig.getMetricType().toUpperCase()))
                    .withExtraParam(extraParam)
                    .build();

            R<RpcStatus> indexResponse = client.createIndex(indexParam);
            if (indexResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("创建索引失败: {}", indexResponse.getMessage());
                return false;
            }

            // 加载集合到内存
            R<RpcStatus> loadResponse = client.loadCollection(
                    LoadCollectionParam.newBuilder().withCollectionName(collectionName).build()
            );
            if (loadResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("加载Collection到内存失败: {}", loadResponse.getMessage());
                return false;
            }

            log.info("Collection {} 创建并加载成功", collectionName);
            return true;
        } catch (Exception e) {
            log.error("创建Collection异常", e);
            return false;
        }
    }

    @Override
    public boolean hasCollection(String collectionName) {
        initialize();
        try {
            HasCollectionParam param = HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            R<Boolean> response = client.hasCollection(param);
            return response.getData() != null && response.getData();
        } catch (Exception e) {
            log.error("检查Collection是否存在异常", e);
            return false;
        }
    }

    @Override
    public boolean dropCollection(String collectionName) {
        initialize();
        try {
            DropCollectionParam param = DropCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            R<RpcStatus> response = client.dropCollection(param);
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            log.error("删除Collection异常", e);
            return false;
        }
    }

    @Override
    public R<?> insert(String collectionName, List<Float[]> vectors, Map<String, Object> metadata) {
        initialize();

        try {
            List<List<Float>> vectorList = vectors.stream()
                    .map(Arrays::asList)
                    .collect(Collectors.toList());

            String content = (String) metadata.getOrDefault("content", "");
            String metaStr = metadata.toString();

            List<String> contentList = Collections.nCopies(vectors.size(), content);
            List<String> metadataList = Collections.nCopies(vectors.size(), metaStr);

            // 创建 Field 对象列表
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("vector", vectorList));
            fields.add(new InsertParam.Field("content", contentList));
            fields.add(new InsertParam.Field("metadata", metadataList));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields) // ✅ 使用 withFields 而不是 addField
                    .build();

            return client.insert(insertParam);
        } catch (Exception e) {
            log.error("插入向量数据异常", e);
            throw new RuntimeException("插入向量数据异常", e);
        }
    }

    @Override
    public SearchResultsWrapper search(String collectionName, List<Float[]> queryVectors, int topK, List<String> outputFields) {
        initialize();

        try {
            List<List<Float>> floatVectors = queryVectors.stream()
                    .map(Arrays::asList)
                    .collect(Collectors.toList());

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(MetricType.valueOf(ragConfig.getMetricType().toUpperCase()))
                    .withOutFields(outputFields)
//                    .addParam("nprobe", "10")
                    .withTopK(topK)
                    .withFloatVectors(floatVectors)
                    .withVectorFieldName("vector")
                    .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                    .build();

            R<SearchResults> response = client.search(searchParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("搜索向量失败: {}", response.getMessage());
                throw new RuntimeException("搜索向量失败: " + response.getMessage());
            }

            SearchResultData results = response.getData().getResults();
            if (results == null) {
                throw new RuntimeException("搜索返回空结果");
            }

            return new SearchResultsWrapper(results);
        } catch (Exception e) {
            log.error("搜索向量异常", e);
            throw new RuntimeException("搜索向量异常", e);
        }
    }

//    @Override
//    public long countEntities(String collectionName) {
//        initialize();
//
//        try {
//            DescribeCollectionParam descParam = DescribeCollectionParam.newBuilder()
//                    .withCollectionName(collectionName)
//                    .build();
//
//            R<DescribeCollectionResponse> response = client.describeCollection(descParam);
//            if (response.getStatus() != R.Status.Success.getCode()) {
//                log.error("获取Collection描述失败: {}", response.getMessage());
//                return 0;
//            }
//
//            DescCollResponseWrapper wrapper = new DescCollResponseWrapper(response.getData());
//            response.getData().
//            return wrapper.getEntityCount(); // ✅ 2.4.6 SDK 中此方法存在
//        } catch (Exception e) {
//            log.error("获取Collection实体数量异常", e);
//            return 0;
//        }
//    }

    @Override
    public String createTenantCollection(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("租户 ID 不能为空");
        }

        String collectionName = ragConfig.getCollectionPrefix() + tenantId;
        if (hasCollection(collectionName)) {
            log.debug("租户 {} 对应的 collection {} 已存在", tenantId, collectionName);
            return collectionName;
        }

        initialize();

        try {
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(36)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build();

            FieldType contentField = FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();

            FieldType sourceField = FieldType.newBuilder()
                    .withName("source")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(255)
                    .build();

            FieldType docTitleField = FieldType.newBuilder()
                    .withName("doc_title")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(500)
                    .build();

            FieldType linkField = FieldType.newBuilder()
                    .withName("link")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(1000)
                    .build();

            FieldType embeddingField = FieldType.newBuilder()
                    .withName("embedding")
                    .withDataType(DataType.FloatVector)
                    .withDimension(ragConfig.getDimension())
                    .build();

            CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                    .addFieldType(idField)
                    .addFieldType(contentField)
                    .addFieldType(sourceField)
                    .addFieldType(docTitleField)
                    .addFieldType(linkField)
                    .addFieldType(embeddingField)
                    .build();

            R<RpcStatus> createR = client.createCollection(
                    CreateCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withSchema(schema)
                            .build()
            );

            if (createR.getStatus() != R.Status.Success.getCode()) {
                log.error("创建租户 {} 的 collection {} 失败: {}", tenantId, collectionName, createR.getMessage());
                throw new RuntimeException("创建 Milvus collection 失败: " + createR.getMessage());
            }

            // 创建索引
            String extraParam = "{\"nlist\":128}";
            R<RpcStatus> indexR = client.createIndex(
                    CreateIndexParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withFieldName("embedding")
                            .withIndexType(IndexType.valueOf(ragConfig.getIndexType().toUpperCase()))
                            .withMetricType(MetricType.valueOf(ragConfig.getMetricType().toUpperCase()))
                            .withExtraParam(extraParam)
                            .build()
            );

            if (indexR.getStatus() != R.Status.Success.getCode()) {
                log.error("为租户 {} 的 collection {} 创建索引失败: {}", tenantId, collectionName, indexR.getMessage());
                throw new RuntimeException("创建 Milvus 索引失败: " + indexR.getMessage());
            }

            // 加载
            R<RpcStatus> loadR = client.loadCollection(
                    LoadCollectionParam.newBuilder().withCollectionName(collectionName).build()
            );
            if (loadR.getStatus() != R.Status.Success.getCode()) {
                log.error("加载Collection到内存失败: {}", loadR.getMessage());
                throw new RuntimeException("加载Collection失败: " + loadR.getMessage());
            }

            log.info("成功创建租户 {} 的 collection {}", tenantId, collectionName);
            return collectionName;
        } catch (Exception e) {
            log.error("创建租户 {} 的 collection 失败", tenantId, e);
            throw new RuntimeException("创建租户 collection 失败", e);
        }
    }

    @Override
    public void insertChunks(String tenantId, List<RagChunk> chunks) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("租户 ID 不能为空");
        }
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        String collectionName = ragConfig.getCollectionPrefix() + tenantId;
        createTenantCollection(tenantId); // 确保存在

        initialize();

        try {
            List<String> ids = new ArrayList<>();
            List<String> contents = new ArrayList<>();
            List<String> sources = new ArrayList<>();
            List<String> docTitles = new ArrayList<>();
            List<String> links = new ArrayList<>();
            List<List<Float>> embeddings = new ArrayList<>();

            for (RagChunk chunk : chunks) {
                String id = UUID.randomUUID().toString();
                ids.add(id);
                contents.add(chunk.getContent());
                sources.add(chunk.getSource());
                docTitles.add(chunk.getDocTitle());
                links.add(chunk.getLink() != null ? chunk.getLink() : "");

                double[] vectorArray = embeddingService.embedText(chunk.getContent());
                List<Float> vectorList = Arrays.stream(vectorArray)
                        .mapToObj(d -> (float) d)
                        .collect(Collectors.toList());
                embeddings.add(vectorList);
            }

            // 构建字段列表
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("content", contents));
            fields.add(new InsertParam.Field("source", sources));
            fields.add(new InsertParam.Field("doc_title", docTitles));
            fields.add(new InsertParam.Field("link", links));
            fields.add(new InsertParam.Field("embedding", embeddings));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)  // ✅ 替代所有 addField
                    .build();

            R<MutationResult> insertR = client.insert(insertParam);
            if (insertR.getStatus() != R.Status.Success.getCode()) {
                log.error("向租户 {} 的 collection {} 插入数据失败: {}", tenantId, collectionName, insertR.getMessage());
                throw new RuntimeException("插入 Milvus 数据失败: " + insertR.getMessage());
            }

            client.flush(FlushParam.newBuilder().addCollectionName(collectionName).build());
            log.info("成功向租户 {} 的 collection {} 插入 {} 条文档片段", tenantId, collectionName, chunks.size());
        } catch (Exception e) {
            log.error("向租户 {} 的 collection 插入数据失败", tenantId, e);
            throw new RuntimeException("插入文档片段失败", e);
        }
    }

    @Override
    public List<RagChunk> retrieve(String query, String tenantId, int topK) {
        if (tenantId == null || tenantId.isBlank()) {
            return Collections.emptyList();
        }

        topK = Math.min(topK, ragConfig.getMaxTopK());
        String collectionName = ragConfig.getCollectionPrefix() + tenantId;

        if (!hasCollection(collectionName)) {
            log.debug("租户 {} 对应的 collection {} 不存在", tenantId, collectionName);
            return Collections.emptyList();
        }

        initialize();

        try {
            double[] queryVectorArray = embeddingService.embedText(query);
            List<Float> queryVector = Arrays.stream(queryVectorArray)
                    .mapToObj(d -> (float) d)
                    .collect(Collectors.toList());

            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
                    .addOutField("content")
                    .addOutField("source")
                    .addOutField("doc_title")
                    .addOutField("link")
                    .withVectorFieldName("embedding")
                    .withTopK(topK)
                    .withRoundDecimal(6)
//                    .addParam("nprobe", "10")
                    .withVectors(Collections.singletonList(queryVector))
                    .build();

            R<SearchResults> searchResultsR = client.search(searchParam);
            if (searchResultsR.getStatus() != R.Status.Success.getCode()) {
                log.warn("Milvus 搜索失败: {}", searchResultsR.getMessage());
                return Collections.emptyList();
            }

            SearchResultData results = searchResultsR.getData().getResults();
            if (results == null) {
                return Collections.emptyList();
            }

            SearchResultsWrapper wrapper = new SearchResultsWrapper(results);
            int queryIndex = 0;

            List<?> contentList = wrapper.getFieldData("content", queryIndex);
            List<?> sourceList = wrapper.getFieldData("source", queryIndex);
            List<?> docTitleList = wrapper.getFieldData("doc_title", queryIndex);
            List<?> linkList = wrapper.getFieldData("link", queryIndex);

            List<RagChunk> chunks = new ArrayList<>();
            for (int i = 0; i < contentList.size(); i++) {
                String content = (String) contentList.get(i);
                String source = (String) sourceList.get(i);
                String docTitle = (String) docTitleList.get(i);
                String link = linkList != null && i < linkList.size() ? (String) linkList.get(i) : "";

                if (content != null) {
                    chunks.add(new RagChunk(content, source, docTitle, link));
                }
            }

            log.debug("RAG 检索完成: 租户={}, 查询={}, 找到 {} 个结果", tenantId, query, chunks.size());
            return chunks;
        } catch (Exception e) {
            log.warn("RAG 检索异常，租户={}，返回空", tenantId, e);
            return Collections.emptyList();
        }
    }
}