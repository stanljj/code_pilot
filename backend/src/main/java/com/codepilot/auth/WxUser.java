package com.codepilot.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 微信小程序用户：由 code2Session 得到 openid 后查找或创建，并关联 tenantId。
 */
@Entity
@Table(name = "wx_user", indexes = {
        @Index(name = "idx_tenant_id", columnList = "tenant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_open_id", columnNames = "open_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_id", nullable = false, length = 64)
    private String openId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
