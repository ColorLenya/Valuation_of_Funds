package com.valuation.funds.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valuation.funds.auth.HscloudTokenHolder;
import com.valuation.funds.auth.TokenResponse;
import com.valuation.funds.config.HscloudOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

/**
 * 应用启动时向恒生开放平台请求 OAuth2 访问令牌，并打印返回结果。
 * 授权方式：client_credentials，Authorization: Basic Base64(app_key:app_secret)。
 * 机制说明
 * ApplicationRunner 是 Spring Boot 的启动回调接口
 * 任何被 Spring 管理的 Bean，只要实现了 ApplicationRunner，Spring 会在应用完全启动之后（上下文已刷新、可以接收请求之前）自动调用它的 run(ApplicationArguments args) 方法。
 * HscloudTokenRunner 满足两点
 * 类上有 @Component，会被扫描并注册为 Bean。
 * 实现了 ApplicationRunner，所以 Spring 在启动阶段会调用它的 run(...)。
 * 执行时机
 * 在 SpringApplication.run(...) 执行过程中，在“上下文就绪”和“应用开始对外服务”之间，Spring 会收集所有 ApplicationRunner 和 CommandLineRunner 的 Bean，并依次执行它们的 run 方法。
 * 所以项目一启动，这段获取恒生 token 的逻辑就会自动跑一次。
 */
@Component
public class HscloudTokenRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HscloudTokenRunner.class);

    private final HscloudOAuthProperties oauthProperties;
    private final HscloudTokenHolder tokenHolder;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HscloudTokenRunner(HscloudOAuthProperties oauthProperties, HscloudTokenHolder tokenHolder) {
        this.oauthProperties = oauthProperties;
        this.tokenHolder = tokenHolder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String appKey = oauthProperties.getAppKey() == null ? null : oauthProperties.getAppKey().trim();
        String appSecret = oauthProperties.getAppSecret() == null ? null : oauthProperties.getAppSecret().trim();
        if (appKey == null || appKey.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            log.warn("恒生 OAuth 未配置 app-key 或 app-secret，跳过获取 token");
            return;
        }

        String tokenUrl = oauthProperties.getTokenUrl();
        // Basic Base64(app_key:app_secret)，UTF-8，区分大小写，含冒号
        String credentials = appKey + ":" + appSecret;
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 与 Postman 一致：表单 body，由 Spring 按 application/x-www-form-urlencoded 序列化
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        log.debug("恒生 OAuth 请求 URL: {}, AppKey 前8位: {}...", tokenUrl, appKey.substring(0, Math.min(8, appKey.length())));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            String result = response.getBody();
            log.info("恒生 OAuth2 授权成功，返回结果：\n{}", result);
            System.out.println("========== 恒生 OAuth2 授权返回 ==========");
            System.out.println(result);
            System.out.println("========================================");
            // 解析并保存 access_token，供后续调用业务 API 使用
            if (result != null && !result.isBlank()) {
                try {
                    TokenResponse tokenResponse = objectMapper.readValue(result, TokenResponse.class);
                    if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                        tokenHolder.setAccessToken(tokenResponse.getAccessToken());
                        log.info("已保存 access_token，后续请求将使用 Bearer 方式携带");
                    }
                } catch (Exception e) {
                    log.warn("解析 token 响应失败，将不保存 token: {}", e.getMessage());
                }
            }
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("恒生 OAuth2 授权失败 HTTP {}: {}", e.getStatusCode(), responseBody, e);
            System.err.println("恒生 OAuth2 授权失败: " + e.getStatusCode() + " " + e.getStatusText());
            if (responseBody != null && !responseBody.isEmpty()) {
                System.err.println("响应内容: " + responseBody);
            } else {
                System.err.println("（无响应体，请检查 App Key / App Secret 是否与开放平台一致、应用是否已启用）");
            }
        } catch (Exception e) {
            log.error("恒生 OAuth2 授权请求失败: {}", e.getMessage(), e);
            System.err.println("恒生 OAuth2 授权失败: " + e.getMessage());
        }
    }
}
