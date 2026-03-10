package com.valuation.funds.service;

import com.valuation.funds.auth.HscloudTokenHolder;
import com.valuation.funds.config.HscloudApiProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 调用恒生基金估值接口：POST，Body 为 secu_codes，Header 为 Authorization: Bearer &lt;token&gt;。
 */
@Service
public class FundValuationService {

    private final HscloudTokenHolder tokenHolder;
    private final HscloudApiProperties apiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public FundValuationService(HscloudTokenHolder tokenHolder, HscloudApiProperties apiProperties) {
        this.tokenHolder = tokenHolder;
        this.apiProperties = apiProperties;
    }

    /**
     * 请求基金估值最新点位。
     *
     * @param secuCodes 证券代码，如 020156
     * @return 恒生接口返回的原始字符串
     */
    public String getFundValuationLastPoint(String secuCodes) {
        if (!tokenHolder.hasToken()) {
            throw new IllegalStateException("未获取到恒生 access_token，请确认启动时 OAuth2 授权已成功");
        }
        String url = apiProperties.getFundValuationUrl();
        String bearerToken = "Bearer " + tokenHolder.getAccessToken().trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", bearerToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secu_codes", secuCodes == null ? "" : secuCodes.trim());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return response.getBody();
    }
}
