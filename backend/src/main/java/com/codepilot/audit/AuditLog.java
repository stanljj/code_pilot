//package com.codepilot.audit;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.Instant;
//
///**
// * 审计日志：谁、何时、查询了什么（满足 SOC2）。
// */
//@Data
//@Entity
//@Table(name = "audit_log", indexes = @Index(name = "idx_tenant_user_time", columnList = "tenantId, userId, createdAt"))
//public class AuditLog {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String tenantId;
//    private String userId;
//    private String requestType;  // public / enterprise
//    @Column(length = 500)
//    private String contentHash;  // 不存原文，仅 hash，便于审计追溯
//    private Instant createdAt;
//
//    @PrePersist
//    public void prePersist() {
//        if (createdAt == null) {
//            createdAt = Instant.now();
//        }
//    }
//}
