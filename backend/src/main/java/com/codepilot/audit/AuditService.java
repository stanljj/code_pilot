//package com.codepilot.audit;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.HexFormat;
//
///**
// * 审计记录：不存原文，仅存 content 的 hash + 租户/用户/时间/请求类型。
// */
//@Service
//public class AuditService {
//
//    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
//
//    private final AuditLogRepository repository;
//
//    @Value("${audit.enabled:true}")
//    private boolean enabled;
//
//    public AuditService(AuditLogRepository repository) {
//        this.repository = repository;
//    }
//
//    public void log(String tenantId, String userId, String requestType, String content) {
//        if (!enabled || repository == null) {
//            return;
//        }
//        try {
//            AuditLog entry = new AuditLog();
//            entry.setTenantId(tenantId);
//            entry.setUserId(userId);
//            entry.setRequestType(requestType);
//            entry.setContentHash(content != null ? sha256(content) : null);
//            repository.save(entry);
//        } catch (Exception e) {
//            log.warn("审计日志写入失败", e);
//        }
//    }
//
//    private static String sha256(String input) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
//            return HexFormat.of().formatHex(digest);
//        } catch (NoSuchAlgorithmException e) {
//            return "hash-error";
//        }
//    }
//}
