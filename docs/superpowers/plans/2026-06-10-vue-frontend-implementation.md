# Vue Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone Vue 3 bookkeeping frontend that connects to the existing Spring Boot API.

**Architecture:** The frontend lives in `frontend/` and is served by Vite on port `5173`. It communicates with the backend only through typed API modules using `VITE_API_BASE_URL`; the backend remains a separate Spring Boot service on port `8080`.

**Tech Stack:** Vue 3, Vite, TypeScript, Vue Router, Pinia, Vitest, Vue Test Utils, Material Symbols, Spring Security CORS.

---

## File Structure

- Modify `src/main/java/com/hwiyn/billrecord/config/SecurityConfig.java`: enable CORS for local Vue development origins.
- Modify `src/test/java/com/hwiyn/billrecord/config/SecurityConfigTest.java`: verify local Vue CORS preflight.
- Create `frontend/package.json`: frontend scripts and dependencies.
- Create `frontend/index.html`, `frontend/vite.config.ts`, `frontend/tsconfig*.json`, `frontend/.env.example`: Vite project configuration.
- Create `frontend/src/api/`: typed API client and feature API wrappers.
- Create `frontend/src/stores/`: Pinia auth and ledger stores.
- Create `frontend/src/router/`: protected route definitions.
- Create `frontend/src/components/`: reusable GitHub-style UI components.
- Create `frontend/src/views/`: login, dashboard, transactions, accounts, categories, budgets, and reports pages.
- Create `frontend/src/styles/`: global GitHub-like styles and responsive layout.
- Create `frontend/src/test/`: Vitest setup and focused tests.

## Tasks

### Task 1: Backend CORS

- [x] Add a failing MockMvc test for `OPTIONS /api/v1/accounts` from `http://localhost:5173`.
- [x] Run `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -q -Dtest=SecurityConfigTest test` and confirm it fails with `401`.
- [x] Enable `http.cors()` and register a `CorsConfigurationSource` for `/api/**`.
- [x] Re-run the same test and confirm it passes.

### Task 2: Vue Project Foundation

- [ ] Create frontend Vite configuration and package files.
- [ ] Add `.env.example` with `VITE_API_BASE_URL=http://localhost:8080/api/v1`.
- [ ] Add Vitest configuration with jsdom and Vue plugin.
- [ ] Run `npm install` inside `frontend/`.
- [ ] Run initial tests and typecheck to verify the toolchain.

### Task 3: Typed API Layer

- [ ] Write failing tests for `apiClient` token attachment, backend error parsing, and `401` session clearing.
- [ ] Implement `src/api/client.ts`.
- [ ] Add typed API modules for auth, users, accounts, categories, transactions, budgets, and reports.
- [ ] Run `npm run test:run`.

### Task 4: Stores And Routing

- [ ] Write failing tests for auth store persistence and protected route redirect.
- [ ] Implement `src/stores/auth.ts`, `src/stores/ledger.ts`, and `src/router/index.ts`.
- [ ] Run `npm run test:run`.

### Task 5: GitHub-Style UI Shell

- [ ] Implement global CSS tokens, app shell, top bar, side nav, button, empty state, modal, table, and form primitives.
- [ ] Use Material Symbols for navigation and actions.
- [ ] Verify the shell renders without route overlap at desktop and mobile widths.

### Task 6: Feature Views

- [ ] Implement login/register.
- [ ] Implement dashboard with monthly summary, account balances, budget progress, recent transactions, category spending, and cash flow.
- [ ] Implement transactions list plus create/edit/delete form.
- [ ] Implement accounts CRUD.
- [ ] Implement categories CRUD.
- [ ] Implement budgets CRUD.
- [ ] Implement reports view.
- [ ] Run `npm run typecheck`, `npm run test:run`, and `npm run build`.

### Task 7: Runtime Verification

- [ ] Start or verify PostgreSQL and Spring Boot backend on `localhost:8080`.
- [ ] Run an HTTP smoke flow against the backend: register, create account, create category, create budget, create transaction, read reports.
- [ ] Start `npm run dev -- --host 127.0.0.1 --port 5173`.
- [ ] Verify `http://localhost:5173` returns the frontend HTML.
- [ ] Open the browser for user review.

### Task 8: GitHub

- [ ] Review `git status` and avoid unrelated changes.
- [ ] Commit the frontend and CORS work.
- [ ] Push to `origin/main`.

