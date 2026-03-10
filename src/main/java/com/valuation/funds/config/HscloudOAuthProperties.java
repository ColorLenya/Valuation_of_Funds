package com.valuation.funds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 恒生开放互联平台 OAuth2 配置。
 * 生产环境建议通过环境变量覆盖 app-key、app-secret。
 */
@Component
@ConfigurationProperties(prefix = "hscloud.oauth")
//表示：把 application.yml 里以 hscloud.oauth 开头的配置项，按“前缀去掉、命名转成驼峰”的规则，绑定到当前类的字段上。
//配置属性类，用来把配置文件里的恒生 OAuth2 相关配置集中映射成一个 Java 对象,bu
public class HscloudOAuthProperties {

    /** 获取访问令牌的 URL */
    //=号后面的字符串会生效，但只有在配置里没配 token-url 时才会用到
        private String tokenUrl = "https://sandbox111111.hscloud.cn/oauth2/oauth2/token";
        //https://open.hscloud.cn/oauth2/oauth2/token
        //https://sandbox.hscloud.cn/oauth2/oauth2/token

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
