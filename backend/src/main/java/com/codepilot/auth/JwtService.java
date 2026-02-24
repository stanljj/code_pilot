package com.codepilot.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析：登录成功后签发，请求 /api/v1/** 时从 Authorization: Bearer &lt;token&gt; 解析出用户与 tenantId。
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String CLAIM_TENANT_ID = "tenantId";

    @Value("${jwt.secret:codepilot-mini-default-secret-change-in-production}")
    private String secret;

    @Value("${jwt.expiration-ms:604800000}")
    private long expirationMs;

    private SecretKey signingKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 签发 JWT，sub=openid，claim 中带 tenantId。
     */
    public String generateToken(String openId, String tenantId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(openId)
                .claim(CLAIM_TENANT_ID, tenantId != null ? tenantId : "default")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /**
     * 解析并校验 JWT，返回 claims；无效或过期返回 null。
     */
    public Claims parseToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT 已过期: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    public String getTenantIdFromClaims(Claims claims) {
        if (claims == null) return null;
        Object tid = claims.get(CLAIM_TENANT_ID);
        return tid != null ? tid.toString() : null;
    }
}
