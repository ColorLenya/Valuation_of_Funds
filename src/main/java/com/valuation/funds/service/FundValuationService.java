package com.valuation.funds.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valuation.funds.auth.HscloudTokenHolder;
import com.valuation.funds.config.HscloudApiProperties;
import com.valuation.funds.entity.FundValuationItem;
import com.valuation.funds.entity.HscloudFundValuationRawResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 调用恒生基金估值接口：POST，Body 为 secu_codes，Header 为 Authorization: Bearer &lt;token&gt;。
 */
@Service
public class FundValuationService {

    private static final Logger log = LoggerFactory.getLogger(FundValuationService.class);

    private final HscloudTokenHolder tokenHolder;
    private final HscloudApiProperties apiProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public FundValuationService(HscloudTokenHolder tokenHolder, HscloudApiProperties apiProperties,
                                ObjectMapper objectMapper) {
        this.tokenHolder = tokenHolder;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 请求基金估值最新点位，并解析为实体列表（便于前端展示与后续入库）。
     *
     * @param secuCodes 证券代码，如 020156
     * @return 解析后的 FundValuationItem 列表，按 result_columns 顺序绑定 target_time、estimated_nav、estimated_change、fundcode
     */
    public List<FundValuationItem> getFundValuationLastPoint(String secuCodes) {
        String raw = getFundValuationLastPointRaw(secuCodes);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            // 恒生可能把 result_columns/result_values 放在根节点，或放在 data/result 等嵌套节点下
            JsonNode dataNode = root;
            if (root.has("data") && root.get("data").isObject()) {
                dataNode = root.get("data");
            } else if (root.has("result") && root.get("result").isObject()) {
                dataNode = root.get("result");
            }
            HscloudFundValuationRawResponse parsed = objectMapper.treeToValue(dataNode, HscloudFundValuationRawResponse.class);
            List<FundValuationItem> list = FundValuationMapper.toFundValuationItems(parsed);
            if (list.isEmpty() && (parsed == null || parsed.getResultValues() == null) && raw.length() < 2000) {
                log.warn("恒生基金估值解析后无数据，请核对返回结构。原始响应（截断）: {}", raw);
            }
            return list;
        } catch (Exception e) {
            throw new IllegalArgumentException("解析恒生返回 JSON 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 请求基金估值最新点位，返回恒生接口原始字符串（供调试等）。
     */
    public String getFundValuationLastPointRaw(String secuCodes) {
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
