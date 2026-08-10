# AGENTS.md — signup-ui

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

React/TypeScript frontend for the signup module. Serves three pages: `/signup` (registration via mobile OTP), `/reset-password` (mobile + full-name OTP verification, then set new password), and `/identity-verification` (video-based KYC, reachable only via redirect from eSignet, not directly). See `README.md` in this directory for the full page-by-page description.

## Layout

- `src/app/AppRouter.tsx` — route definitions.
- `src/assets` — SVG/PNG assets.
- `public/locales/*.json` — translation files (`en.json`, `km.json`, `default.json`); add new keys here and in the corresponding `resource.d.ts` type declarations.
- `public/theme/variables.css`, `public/theme-config.js`, `public/lang-config.js` — runtime theming/i18n configuration injected at container start.
- `.storybook/` — Storybook 7 configuration for isolated component development.
- `craco.config.js` — Create React App override (path aliases, webpack tweaks) via `@craco/craco`.
- `nginx/nginx.conf` — production nginx config; listens on port `3000` (matches `Dockerfile`'s `EXPOSE 3000`).
- `configure_start.sh` — Docker entrypoint that stamps runtime environment (theme/lang/favicon/title) into `env.env` before nginx starts.

Registration form fields are driven by `@mosip/json-form-builder` from a UI-spec JSON returned by the backend (since v1.3.0) — see `../docs/design/dynamic-forms.md` for how that integration works before changing form-rendering logic.

## How to run

Install dependencies:

```bash
npm install
```

Local dev server (opens `http://localhost:3000`):

```bash
npm start
```

Storybook (opens `http://localhost:6006`):

```bash
npm run storybook
```

Tests:

```bash
npm test
```

Lint:

```bash
npm run lint
npm run lint:fix
```

Production build:

```bash
npm run build
```

### Local environment

Copy `.env.example` to a self-created `.env.local` and adjust as needed. The only variable defined today is:

| Variable | Default |
|---|---|
| `REACT_APP_API_BASE_URL` | `http://localhost:8088/v1/signup` |

During development, `README.md` recommends running Chrome with `--disable-web-security` to avoid CORS errors against a locally-running backend, since `signup-service`'s default port (`8089`) differs from the API base URL's port shown above — confirm the actual backend port in your environment before relying on the CORS workaround.

## Agent rules

### Do

1. Add new translation keys to every locale file under `public/locales/` and to the matching `resource.d.ts` type declaration, not just `en.json`.
2. Consult `../docs/design/dynamic-forms.md` before modifying how the registration form renders fields from the UI-spec JSON.
3. Run `npm run lint` and `npm test` before opening a PR that touches `src/`.

### Do not

1. Do not commit `.env.local` — it is a local-only override file.
2. Do not change the nginx `listen` port in `nginx/nginx.conf` without also updating `EXPOSE` in `Dockerfile` and the corresponding Helm chart/service definitions under `../helm/signup-ui`.
