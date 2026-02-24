package com.codepilot.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 从 JWT 解析出的用户信息，供 /api/v1/ask 等接口使用；可从中取 tenantId 做 RAG 隔离。
 */
public class WxUserDetails implements UserDetails {

    private final String openId;
    private final String tenantId;

    public WxUserDetails(String openId, String tenantId) {
        this.openId = openId;
        this.tenantId = tenantId != null ? tenantId : "default";
    }

    @Override
    public String getUsername() {
        return openId;
    }

    public String getOpenId() {
        return openId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
