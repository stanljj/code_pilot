package com.codepilot.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 提问响应：问题分析、建议、代码、文档引用、分享卡片链接。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AskResponse {

    /** 请求类型：public / enterprise */
    private String requestType;

    /** 问题分析摘要 */
    private String analysis;

    /** 修复/实现建议 */
    private String suggestion;

    /** 高亮显示的代码片段（可为 null） */
    private String code;

    /** 相关文档引用，如《用户服务规范 v2.1》 */
    private List<DocRef> docRefs;

    /** 分享卡片 URL（企微等） */
    private String cardUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocRef {
        private String title;
        private String link;
    }
}
