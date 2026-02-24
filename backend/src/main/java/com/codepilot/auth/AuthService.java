package com.codepilot.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 登录流程：验证 code → 获取 openid → 查找/创建用户并关联 tenantId → 签发 JWT。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String DEFAULT_TENANT_ID = "default";

    private final WeChatAuthService weChatAuthService;
    private final WxUserRepository wxUserRepository;
    private final JwtService jwtService;

    public AuthService(WeChatAuthService weChatAuthService,
                      WxUserRepository wxUserRepository,
                      JwtService jwtService) {
        this.weChatAuthService = weChatAuthService;
        this.wxUserRepository = wxUserRepository;
        this.jwtService = jwtService;
    }

    /**
     * 小程序传入 code，校验后查找或创建用户，返回 JWT。
     *
     * @param code 微信 wx.login() 得到的 code
     * @return JWT token，失败返回 null（调用方可根据 null 返回 401/400）
     */
    @Transactional
    public String login(String code) {
        String openId = weChatAuthService.getOpenIdByCode(code);
        if (openId == null || openId.isBlank()) {
            log.warn("code 校验失败或未配置微信小程序密钥");
            return null;
        }
        WxUser user = wxUserRepository.findByOpenId(openId)
                .orElseGet(() -> createUser(openId));
        String tenantId = user.getTenantId() != null ? user.getTenantId() : DEFAULT_TENANT_ID;
        return jwtService.generateToken(user.getOpenId(), tenantId);
    }

    private WxUser createUser(String openId) {
        WxUser newUser = WxUser.builder()
                .openId(openId)
                .tenantId(DEFAULT_TENANT_ID)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        newUser = wxUserRepository.save(newUser);
        log.info("新用户已创建: openId={}, tenantId={}", openId, DEFAULT_TENANT_ID);
        return newUser;
    }
}
