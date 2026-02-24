package com.codepilot.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 微信小程序用户仓储：按 openid 查找，用于登录时查找/创建用户并关联 tenantId。
 */
public interface WxUserRepository extends JpaRepository<WxUser, Long> {

    Optional<WxUser> findByOpenId(String openId);
}
