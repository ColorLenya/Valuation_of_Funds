package com.valuation.funds.auth;

import org.springframework.stereotype.Component;

/**
 * 持有恒生 OAuth2 的 access_token，供调用业务 API 时使用。
 */
@Component
public class HscloudTokenHolder {

    private volatile String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean hasToken() {
        return accessToken != null && !accessToken.isBlank();
    }
}
