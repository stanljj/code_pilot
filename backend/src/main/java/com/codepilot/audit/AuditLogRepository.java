//package com.codepilot.audit;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.Instant;
//import java.util.List;
//
//public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
//
//    List<AuditLog> findByTenantIdAndUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
//            String tenantId, String userId, Instant since);
//}
