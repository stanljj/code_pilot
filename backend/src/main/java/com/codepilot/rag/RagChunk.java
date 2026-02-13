package com.codepilot.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG 检索得到的一条知识片段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagChunk {

    private String content;
    private String source;   // 如 Git/Jira/Confluence
    private String docTitle; // 如《用户服务规范 v2.1》
    private String link;
}
