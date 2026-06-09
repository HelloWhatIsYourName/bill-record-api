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

Implemented APIs use base path `/api/v1` and bearer authentication after
register/login.

- Auth/profile: register, login, current profile.
- Accounts: create, list, get, update, archive.
- Categories: create, list, get, update, archive. Default income/expense
  categories are seeded on registration.
- Transactions: create income, expense, transfer; list/filter, get, update,
  delete. Account balances are updated and reversed on changes.
- Budgets: create, list by month, update, delete with spent/remaining values.
- Reports: monthly summary, category spending, cash flow.

Runnable examples are in `docs/http/auth.http`. OpenAPI JSON is available at
`GET /v3/api-docs`.

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

已实现 API 统一使用 `/api/v1` 前缀，注册/登录后用 Bearer Token 访问。

- 认证/资料：注册、登录、当前用户资料。
- 账户：创建、列表、详情、更新、归档。
- 分类：创建、列表、详情、更新、归档。注册时会自动创建默认收入/支出分类。
- 交易：创建收入、支出、转账；列表筛选、详情、更新、删除。账户余额会随交易变更自动正反向调整。
- 预算：创建、按月份列表、更新、删除，并返回已花费/剩余金额。
- 报表：月度汇总、分类支出、现金流。

可直接运行的示例在 `docs/http/auth.http`。OpenAPI JSON 地址为
`GET /v3/api-docs`。
