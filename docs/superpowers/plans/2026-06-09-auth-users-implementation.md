# Auth and Users Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add JWT-secured user registration, login, and current-profile API.

**Architecture:** Keep authentication in the `security` package, user business
rules in `service`, HTTP contracts in `controller`/`dto`, persistence in
`entity`/`repository`, and error translation in `exception`. The flow is
controller -> service -> repository, with MapStruct converting `User` to
`UserResponse`.

**Tech Stack:** Spring Boot 3.3, Spring Security, Spring Data JPA, Flyway,
jjwt, MapStruct, Lombok, JUnit 5, MockMvc, Testcontainers PostgreSQL.

---

## File Map

- Create `src/main/resources/db/migration/V1__create_users.sql`: PostgreSQL user table.
- Create `src/main/java/com/hwiyn/billrecord/entity/User.java`: JPA user entity.
- Create `src/main/java/com/hwiyn/billrecord/repository/UserRepository.java`: email/id lookups.
- Create `src/main/java/com/hwiyn/billrecord/dto/auth/RegisterRequest.java`: register input.
- Create `src/main/java/com/hwiyn/billrecord/dto/auth/LoginRequest.java`: login input.
- Create `src/main/java/com/hwiyn/billrecord/dto/auth/AuthResponse.java`: token output.
- Create `src/main/java/com/hwiyn/billrecord/dto/user/UserResponse.java`: profile output.
- Create `src/main/java/com/hwiyn/billrecord/mapper/UserMapper.java`: entity -> DTO.
- Create `src/main/java/com/hwiyn/billrecord/service/AuthService.java`: registration/login/me.
- Create `src/main/java/com/hwiyn/billrecord/security/UserPrincipal.java`: authenticated principal.
- Create `src/main/java/com/hwiyn/billrecord/security/JwtAuthenticationFilter.java`: bearer token parsing.
- Create `src/main/java/com/hwiyn/billrecord/config/SecurityConfig.java`: stateless security chain and password encoder.
- Create `src/main/java/com/hwiyn/billrecord/controller/AuthController.java`: `/auth` endpoints.
- Create `src/main/java/com/hwiyn/billrecord/controller/UserController.java`: `/users/me`.
- Create `src/main/java/com/hwiyn/billrecord/exception/ApiErrorResponse.java`: error JSON.
- Create `src/main/java/com/hwiyn/billrecord/exception/ApiException.java`: status/code exception.
- Create `src/main/java/com/hwiyn/billrecord/exception/GlobalExceptionHandler.java`: validation/auth errors.
- Create `src/test/java/com/hwiyn/billrecord/service/AuthServiceTest.java`: service unit tests.
- Create `src/test/java/com/hwiyn/billrecord/controller/AuthControllerTest.java`: MockMvc API tests.
- Create `src/test/java/com/hwiyn/billrecord/integration/AuthFlowIntegrationTest.java`: Testcontainers auth flow.
- Modify `README.md`: document implemented auth HTTP examples.

## Tasks

### Task 1: User Persistence

- [ ] Add a failing repository/migration integration test named
  `AuthFlowIntegrationTest.registerPersistsUserInPostgres`.
- [ ] Run `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -q -Dtest=AuthFlowIntegrationTest test`.
- [ ] Add `V1__create_users.sql`, `User`, and `UserRepository`.
- [ ] Re-run the same test and confirm it passes or skips only when Docker is unavailable.
- [ ] Commit: `feat: add user persistence`.

### Task 2: Auth Service

- [ ] Add failing tests in `AuthServiceTest` for successful register, duplicate
  email rejection, successful login, and bad password rejection.
- [ ] Run `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -q -Dtest=AuthServiceTest test`.
- [ ] Implement request/response DTOs, `UserMapper`, `ApiException`, and
  `AuthService`.
- [ ] Re-run `AuthServiceTest`.
- [ ] Commit: `feat: add auth service`.

### Task 3: Security and Controllers

- [ ] Add failing `AuthControllerTest` cases for register, login, `/users/me`,
  validation error format, and protected-route rejection without bearer token.
- [ ] Run `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -q -Dtest=AuthControllerTest test`.
- [ ] Implement `SecurityConfig`, `JwtAuthenticationFilter`, `UserPrincipal`,
  `AuthController`, `UserController`, and `GlobalExceptionHandler`.
- [ ] Re-run `AuthControllerTest`.
- [ ] Commit: `feat: expose auth endpoints`.

### Task 4: Integration and Docs

- [ ] Extend `AuthFlowIntegrationTest` to cover register -> login -> `/users/me`
  through MockMvc with PostgreSQL Testcontainers.
- [ ] Update `README.md` and `docs/http/auth.http` with bilingual examples for
  register, login, and current profile.
- [ ] Run `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -q test`.
- [ ] Commit: `docs: add auth API examples`.
