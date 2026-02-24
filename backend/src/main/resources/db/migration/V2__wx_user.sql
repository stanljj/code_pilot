-- 微信小程序用户：openid 与租户关联，用于 /auth/login 后签发 JWT
CREATE TABLE IF NOT EXISTS wx_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(64) NOT NULL COMMENT '微信 openid',
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '关联租户，用于 RAG 隔离',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_open_id (open_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
