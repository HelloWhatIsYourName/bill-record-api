# bill-record-api

## English

Personal bookkeeping and finance backend API.

### Stack

Java 21, Spring Boot 3.3, Spring Data JPA, PostgreSQL 16, Flyway, Spring Security,
JWT (jjwt), springdoc-openapi, MapStruct, Lombok, Maven, JUnit 5,
Testcontainers, Docker, and Docker Compose.

### Local Development

```bash
docker compose up -d postgres
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw test
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

Swagger UI will be available at `http://localhost:8080/swagger-ui.html` after
the application starts.

### HTTP Examples

The domain API is still being implemented. The OpenAPI document will be exposed
at `GET /v3/api-docs` once controllers are added.

## 中文

个人记账理财后端 API。

### 技术栈

Java 21、Spring Boot 3.3、Spring Data JPA、PostgreSQL 16、Flyway、
Spring Security、JWT (jjwt)、springdoc-openapi、MapStruct、Lombok、Maven、
JUnit 5、Testcontainers、Docker 和 Docker Compose。

### 本地开发

```bash
docker compose up -d postgres
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw test
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

应用启动后，Swagger UI 地址为 `http://localhost:8080/swagger-ui.html`。

### HTTP 示例

业务 API 正在实现中。控制器添加后，OpenAPI 文档将通过 `GET /v3/api-docs`
暴露。
