package com.valuation.funds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 恒生开放互联平台 OAuth2 配置。
 * 生产环境建议通过环境变量覆盖 app-key、app-secret。
 */
@Component
@ConfigurationProperties(prefix = "hscloud.oauth")
public class HscloudOAuthProperties {

    /** 获取访问令牌的 URL */
    private String tokenUrl = "https://open.hscloud.cn/oauth2/oauth2/token";

    /** 应用 Key（app_key） */
    private String appKey;

    /** 应用 Secret（app_secret） */
    private String appSecret;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
