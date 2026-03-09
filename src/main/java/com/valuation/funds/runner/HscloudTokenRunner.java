package com.valuation.funds.runner;

import com.valuation.funds.config.HscloudOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 应用启动时向恒生开放平台请求 OAuth2 访问令牌，并打印返回结果。
 * 授权方式：client_credentials，Authorization: Basic Base64(app_key:app_secret)。
 */
@Component
public class HscloudTokenRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HscloudTokenRunner.class);

    private final HscloudOAuthProperties oauthProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public HscloudTokenRunner(HscloudOAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String appKey = oauthProperties.getAppKey();
        String appSecret = oauthProperties.getAppSecret();
        if (appKey == null || appKey.isBlank() || appSecret == null || appSecret.isBlank()) {
            log.warn("恒生 OAuth 未配置 app-key 或 app-secret，跳过获取 token");
            return;
        }

        String tokenUrl = oauthProperties.getTokenUrl();
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((appKey + ":" + appSecret).getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

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
        } catch (Exception e) {
            log.error("恒生 OAuth2 授权请求失败: {}", e.getMessage(), e);
            System.err.println("恒生 OAuth2 授权失败: " + e.getMessage());
        }
    }
}
