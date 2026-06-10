# Vue Frontend Design

## Goal

Build an independent Vue frontend for the bill record API. The frontend runs as
its own Vite app on `localhost:5173`, talks to the Spring Boot backend on
`localhost:8080`, and stays deployable as a static app without being bundled into
the backend jar.

## Requirements

- Create the app under `frontend/`.
- Use Vue 3, Vite, TypeScript, Vue Router, and Pinia.
- Use `VITE_API_BASE_URL` for the backend URL. The default local value is
  `http://localhost:8080/api/v1`.
- Use Bearer token authentication from the backend auth API.
- Use Material Symbols from an npm package, not a runtime CDN.
- Use a concise GitHub-like interface: white background, gray borders, compact
  navigation, dense tables, restrained forms, and no marketing landing page.
- Support local development with frontend on `localhost:5173` and backend on
  `localhost:8080`.
- Keep the frontend and backend build lifecycles separate.

## Architecture

The frontend is a standalone Vite application in `frontend/`. It owns browser UI
state, form state, routing, and the stored access token. The backend remains the
source of truth for identities, balances, budget calculations, and reports.

API access is isolated in `src/api/`. A shared `apiClient` reads
`VITE_API_BASE_URL`, attaches the current Bearer token, parses backend error
payloads, and clears the session on `401`. Feature modules expose typed
functions for auth, profile, accounts, categories, transactions, budgets, and
reports.

Pinia stores keep state narrow:

- `auth`: token, user, login, register, logout, and session bootstrap.
- `ledger`: accounts, categories, transactions, budgets, reports, and reload
  actions used by the dashboard and management pages.

Views are routed through `vue-router`:

- `/login`: combined login/register panel.
- `/`: dashboard with monthly totals, account balances, budget progress, recent
  transactions, and cash-flow/category summaries.
- `/transactions`: list, filters, create/update/delete transaction drawer.
- `/accounts`: account CRUD.
- `/categories`: category CRUD.
- `/budgets`: monthly budget CRUD.
- `/reports`: report-focused view for category spending and cash flow.

## UI Design

The app opens directly to the work area after login. The shell has a top bar for
identity/actions, a left navigation rail, and a main content area. Components use
GitHub-like primitives: `.Box`, `.Table`, `.Button`, `.Label`, `.Subhead`, and
form rows. Cards are used only for individual panels or repeated items, not as
nested decoration.

Material Symbols provide navigation and action icons such as `dashboard`,
`receipt_long`, `account_balance_wallet`, `category`, `savings`, `analytics`,
`add`, `edit`, `delete`, and `logout`.

The layout is responsive: on smaller screens, navigation becomes a horizontal
scroll strip and tables collapse into compact rows where needed.

## Data Flow

1. User logs in or registers through `/api/v1/auth`.
2. `auth` store persists the access token in `localStorage`.
3. Protected routes load `/api/v1/users/me`; missing or invalid token redirects
   to `/login`.
4. Dashboard and feature pages load accounts/categories first, then dependent
   resources such as transactions, budgets, and reports.
5. Mutations call the API, then refresh the affected store slices.

## Error Handling

Backend errors use the existing `ApiErrorResponse` shape. The frontend displays
the backend message and field errors when present. Network failures display a
short connection message that includes the configured API base URL. Unauthorized
responses clear the token and return to `/login`.

## Testing

- Unit tests cover API client behavior, auth store token handling, formatting
  helpers, and at least one core dashboard/component render path.
- `npm run typecheck` validates TypeScript.
- `npm run test:run` runs Vitest.
- `npm run build` proves the Vue app compiles.
- Backend `SecurityConfigTest` covers local frontend CORS preflight.
- A runtime smoke test verifies the backend responds and the frontend dev server
  serves the app on `localhost:5173`.

