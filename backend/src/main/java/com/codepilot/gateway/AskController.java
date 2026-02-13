package com.codepilot.gateway;

import com.codepilot.audit.AuditService;
import com.codepilot.classify.RequestClassifier;
import com.codepilot.classify.RequestType;
import com.codepilot.fusion.FusionService;
import com.codepilot.llm.LlmResult;
import com.codepilot.llm.LlmService;
import com.codepilot.rag.RagChunk;
import com.codepilot.rag.RagService;
import com.codepilot.security.DesensitizeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统一提问入口：分类 → LLM/RAG → 融合 → 脱敏 → 审计 → 返回。
 */
@RestController
@RequestMapping("/api/v1")
public class AskController {

    private final RequestClassifier classifier;
    private final LlmService llmService;
    private final RagService ragService;
    private final FusionService fusionService;
    private final DesensitizeService desensitizeService;
    private final AuditService auditService;

    public AskController(RequestClassifier classifier,
                         LlmService llmService,
                         RagService ragService,
                         FusionService fusionService,
                         DesensitizeService desensitizeService,
                         AuditService auditService) {
        this.classifier = classifier;
        this.llmService = llmService;
        this.ragService = ragService;
        this.fusionService = fusionService;
        this.desensitizeService = desensitizeService;
        this.auditService = auditService;
    }

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AskResponse> ask(
            @Valid @RequestBody AskRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        String tenantId = resolveTenantId(user);
        RequestType type = classifier.classify(request.getContent(), tenantId);

        List<RagChunk> ragChunks = List.of();
        if (type == RequestType.ENTERPRISE && tenantId != null) {
            ragChunks = ragService.retrieve(request.getContent(), tenantId, 5);
        }
        String context = ragChunks.isEmpty() ? null : buildContext(ragChunks);

        LlmResult llmResult = llmService.chat(request.getContent(), context);
        AskResponse response = fusionService.fuse(llmResult, ragChunks, type.name());

        response.setAnalysis(desensitizeService.desensitize(response.getAnalysis()));
        response.setSuggestion(desensitizeService.desensitize(response.getSuggestion()));
        if (response.getCode() != null) {
            response.setCode(desensitizeService.desensitize(response.getCode()));
        }

        auditService.log(tenantId, user != null ? user.getUsername() : "anonymous", type.name(), request.getContent());

        return ResponseEntity.ok(response);
    }

    private String resolveTenantId(UserDetails user) {
        if (user == null) return null;
        // 可从 UserDetails 扩展属性或 JWT 中取 tenantId
        return null;
    }

    private String buildContext(List<RagChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (RagChunk c : chunks) {
            sb.append("【").append(c.getDocTitle()).append("】").append(c.getContent()).append("\n");
        }
        return sb.toString();
    }
}
