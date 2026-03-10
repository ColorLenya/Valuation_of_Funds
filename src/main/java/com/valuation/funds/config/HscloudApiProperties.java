package com.valuation.funds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 恒生开放平台业务 API 地址配置。
 */
@Component
@ConfigurationProperties(prefix = "hscloud.api")
public class HscloudApiProperties {

    /** 基金估值最新点位接口 URL */
    private String fundValuationUrl;//= "https://sandbox.hscloud.cn/gildatafund/v1/fundestimate/fundvaluation_lastpoint";

    public String getFundValuationUrl() {
        return fundValuationUrl;
    }

    public void setFundValuationUrl(String fundValuationUrl) {
        this.fundValuationUrl = fundValuationUrl;
    }
}
