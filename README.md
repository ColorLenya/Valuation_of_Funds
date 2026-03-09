# Valuation of Funds

小梵基金估值 API — 基于 Spring Boot 3 的后端服务。

## 环境要求

- **JDK 17**（Spring Boot 3.x 需要）
- Maven 3.6+

请确保 `JAVA_HOME` 指向 JDK 17，否则 Maven 编译会报错（class file version 61.0 / 52.0）。

## 快速开始

```bash
# 编译
mvn compile

# 运行
mvn spring-boot:run
```

启动后访问：<http://localhost:8080/health> 可检查服务状态。

## 恒生开放平台 OAuth2 授权

项目启动时会自动向恒生开放互联平台申请访问令牌（client_credentials 模式），并在控制台打印返回结果。

- **授权地址**：`https://open.hscloud.cn/oauth2/oauth2/token`（POST，application/x-www-form-urlencoded）
- **请求方式**：Header `Authorization: Basic Base64(app_key:app_secret)`，Body `grant_type=client_credentials`
- **配置项**（`application.yml` 中的 `hscloud.oauth`）：
  - `token-url`：令牌接口地址
  - `app-key`：应用 Key
  - `app-secret`：应用 Secret

生产环境建议通过环境变量覆盖敏感信息，例如：

```yaml
hscloud:
  oauth:
    app-key: ${HS_APP_KEY:}
    app-secret: ${HS_APP_SECRET:}
```

## 项目结构

```
src/main/java/com/valuation/funds/
├── ValuationOfFundsApplication.java   # 启动类
├── config/
│   └── HscloudOAuthProperties.java    # 恒生 OAuth2 配置
├── controller/
│   └── HealthController.java          # 健康检查接口
└── runner/
    └── HscloudTokenRunner.java        # 启动时获取恒生 token 并打印
```
