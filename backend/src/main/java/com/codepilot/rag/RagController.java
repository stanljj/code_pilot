package com.codepilot.rag;

import io.milvus.param.R;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final MilvusService milvusService;

    /**
     * 初始化Milvus客户端
     */
    @PostMapping("/init")
    public Map<String, Object> initMilvus() {
        try {
            milvusService.initialize();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Milvus客户端初始化成功");
            return response;
        } catch (Exception e) {
            log.error("初始化Milvus客户端失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Milvus客户端初始化失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 创建Collection
     */
    @PostMapping("/collection/create")
    public Map<String, Object> createCollection(@RequestParam String collectionName,
                                               @RequestParam(defaultValue = "1536") int dimension) {
        try {
            boolean success = milvusService.createCollection(collectionName, dimension);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Collection创建成功" : "Collection创建失败");
            return response;
        } catch (Exception e) {
            log.error("创建Collection失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建Collection失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 检查Collection是否存在
     */
    @GetMapping("/collection/exists/{collectionName}")
    public Map<String, Object> hasCollection(@PathVariable String collectionName) {
        try {
            boolean exists = milvusService.hasCollection(collectionName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            return response;
        } catch (Exception e) {
            log.error("检查Collection是否存在失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "检查Collection是否存在失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除Collection
     */
    @DeleteMapping("/collection/delete/{collectionName}")
    public Map<String, Object> dropCollection(@PathVariable String collectionName) {
        try {
            boolean success = milvusService.dropCollection(collectionName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Collection删除成功" : "Collection删除失败");
            return response;
        } catch (Exception e) {
            log.error("删除Collection失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除Collection失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 插入向量数据
     */
    @PostMapping("/insert")
    public Map<String, Object> insert(@RequestParam String collectionName,
                                      @RequestBody Map<String, Object> requestData) {
        try {
            List<Float[]> vectors = (List<Float[]>) requestData.get("vectors");
            Map<String, Object> metadata = (Map<String, Object>) requestData.get("metadata");

            R<?> result = milvusService.insert(collectionName, vectors, metadata);
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getStatus() == 0);
            response.put("message", result.getMessage());
            response.put("result", result.getData());
            return response;
        } catch (Exception e) {
            log.error("插入向量数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "插入向量数据失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 搜索相似向量
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestParam String collectionName,
                                      @RequestBody Map<String, Object> requestData) {
        try {
            List<Float[]> queryVectors = (List<Float[]>) requestData.get("queryVectors");
            Integer topK = (Integer) requestData.getOrDefault("topK", 10);
            List<String> outputFields = (List<String>) requestData.getOrDefault("outputFields", List.of("content", "metadata"));

            SearchResultsWrapper results = milvusService.search(collectionName, queryVectors, topK, outputFields);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results.getFieldData("content", 0)); // 示例返回内容字段
            response.put("count", results.getRowRecords().size());
            return response;
        } catch (Exception e) {
            log.error("搜索向量失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索向量失败: " + e.getMessage());
            return response;
        }
    }

//    /**
//     * 获取Collection中的向量总数
//     */
//    @GetMapping("/count/{collectionName}")
//    public Map<String, Object> countEntities(@PathVariable String collectionName) {
//        try {
//            long count = milvusService.countEntities(collectionName);
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("count", count);
//            return response;
//        } catch (Exception e) {
//            log.error("获取Collection实体数量失败", e);
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", false);
//            response.put("message", "获取Collection实体数量失败: " + e.getMessage());
//            return response;
//        }
//    }
}