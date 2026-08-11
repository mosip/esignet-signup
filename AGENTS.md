# AGENTS.md

## Repository Overview

`esignet-signup` provides the self-service **signup** flow used alongside [eSignet](https://github.com/mosip/esignet). It lets a resident register for an account (or reset a password) via mobile-number OTP verification, and it supports an optional online video-based identity-verification (KYC) workflow. The signup service talks to a pluggable ID registry through a well-defined SPI, so it can be adapted to different identity systems.

The repository is a **polyrepo-style monorepo**: a Java/Spring Boot backend, a React/TypeScript frontend, plugin-loader Docker images, Helm charts, deployment scripts, and two separate automated test rigs all live here side by side, each with its own build tooling.

Top-level layout (verified against `upstream/develop`):

| Path | What it is |
|---|---|
| `signup-integration-api` | Java library: the SPI (`IdentityVerifierPlugin`, `ProfileRegistryPlugin`) and DTOs that plugins implement against. Part of the root Maven reactor. |
| `signup-service` | The Spring Boot backend service. Part of the root Maven reactor. See [signup-service/AGENTS.md](signup-service/AGENTS.md). |
| `signup-ui` | React/TypeScript frontend (registration, reset-password, identity-verification pages). See [signup-ui/AGENTS.md](signup-ui/AGENTS.md). |
| `signup-with-plugins` | Docker build assets that bundle `signup-service` with preloaded plugin JARs (mock / MOSIP-ID). Own Maven reactor, **not** part of the root build. See [signup-with-plugins/AGENTS.md](signup-with-plugins/AGENTS.md). |
| `deploy` | Shell scripts that install signup and its Kubernetes prerequisites (Keycloak, Kafka, kernel services) via the Helm charts in `helm/`. See [deploy/AGENTS.md](deploy/AGENTS.md). |
| `helm` | Helm charts for `signup-service` and `signup-ui` (each chart has its own `README.md`). |
| `api-test` | REST-Assured/TestNG API test rig. Has its own **`api-test/CLAUDE.md`** — read that instead of duplicating it here. |
| `ui-test` | Selenium/Cucumber/TestNG UI automation rig. See `ui-test/README.md` for setup and `config.properties` details. |
| `performance-test` | JMeter-based performance test assets. |
| `postman-collection` | Postman collection + `create-signup-oidc-keystore.sh` used for manual OIDC client onboarding (see root `README.md`). |
| `docs` | Design docs (dynamic forms, identity-verification, pluggable ID registry) and the OpenAPI spec (`docs/esignet-signup-openapi.yaml`). |

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.3, Spring Cloud 2023.0.3, Maven (multi-module reactor at the repo root).
- **Frontend**: React 18 + TypeScript, `craco` (Create React App wrapper), Tailwind CSS, Storybook 7, Jest.
- **Test rigs**: TestNG + REST Assured (`api-test`), TestNG + Cucumber + Selenium WebDriver (`ui-test`), JMeter (`performance-test`).
- **Deployment**: Helm 3 charts, Docker, shell scripts under `deploy/`.
- **CI**: GitHub Actions, using shared reusable workflows from `mosip/kattu`.

## Build & Test Commands

Root reactor (`signup-integration-api` + `signup-service` only — confirmed by the root `pom.xml` `<modules>` list):

```bash
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

`signup-with-plugins`, `api-test`, and `ui-test` each have their own standalone `pom.xml` (parented to `signup-parent` but **not** listed as root `<module>` entries), so they must be built from their own directory, e.g.:

```bash
cd signup-with-plugins && mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

See the per-module guides linked above for exact commands.

## Configuration

- Backend local overrides: `signup-service/src/main/resources/application-local.properties` (has placeholder values such as `mosip.signup.client.secret=secret-from-env` — replace with real values locally, never commit real secrets there).
- Backend default profile: `signup-service/src/main/resources/application-default.properties`.
- Frontend local overrides: copy `signup-ui/.env.example` into a self-created `signup-ui/.env.local` (per `signup-ui/README.md`) — do not commit `.env.local`.
- Kubernetes secrets (e.g. `signup-keystore`, `signup-captcha`, DB/Redis credentials) are created with `kubectl create secret` from the `deploy/*.sh` scripts, not by hand-editing Helm `values.yaml`. Reserve `helm --set` for non-sensitive chart values only — `--set` values can leak via shell history, process arguments, CI logs, and Helm release metadata — see [deploy/AGENTS.md](deploy/AGENTS.md).

## Project Structure Notes

- `signup-integration-api` defines the plugin contract; actual plugin implementations (e.g. `esignet-mock-plugin`, `mosip-identity-plugin`) live in the separate `mosip/esignet-plugins` repository and are pulled in as JARs, not source, by `signup-with-plugins`.
- `signup-service/configure_start.sh` is the container entrypoint: it downloads/copies the configured plugin JAR onto the classpath (`plugin_name_env`) before starting the Spring Boot app — this is how a single base image supports multiple ID-registry plugins.
- Both `helm/signup-service` and `helm/signup-ui` ship an identical `mock-auth-data/` folder (mock policy/key JSON used for local Mock Identity System integration) — this duplication is intentional per the existing charts, not a mistake to "fix" by deleting one copy.

## Development Workflow

1. Fork and branch from `develop` (the active integration branch — `master` is only used for GA releases).
2. Run the relevant module's build/test commands (see the module guides) before opening a PR.
3. Match the existing commit-message convention: `<JIRA-or-issue-ref>: <summary>`.
4. Sign commits (`git commit -s`) — sign-off is expected by MOSIP maintainers even where not enforced by a bot.

## Pull Request Guidelines

- Target the `develop` branch.
- CI (`.github/workflows/push-trigger.yml`) builds `signup-service`+`signup-integration-api`, `signup-with-plugins`, `api-test`, and `ui-test` as separate jobs, and builds Docker images for `signup-service`, `signup-with-plugins`, and `signup-ui`. A change confined to one module still triggers the full workflow (there are no `paths:` filters on `push-trigger.yml`), so keep an eye on all job results, not just the one for the module you touched.
- `.github/workflows/chart-lint-publish.yml` only runs on changes under `helm/**`, and lints/publishes charts to `mosip-helm`'s `gh-pages` branch on release.
- Keep PRs scoped to one module where possible; the reactor and standalone builds are already split by CI, so a mixed backend+frontend PR will exercise both pipelines.

## Repository-Specific Considerations

- `signup-service` requires MOSIP kernel modules (`otpmanager`, `authmanager`, `auditmanager`, `notifier`) to be reachable; local runs need `MOSIP_API_INTERNAL_HOST` pointed at a real MOSIP (LTS) environment — there is no fully offline mode for the full registration flow, though the Mock Identity System plugin removes the need for a live ID registry specifically.
- The identity-verification (KYC) flow uses WebSockets/STOMP and Redis-backed transaction caches with specific TTL/eviction rules documented in `signup-service/README.md` — read that table before changing cache-key names or eviction points.
- License is MPL 2.0 (`LICENSE`) — new source files should carry the same header used by existing files (see the top of `pom.xml` or any `*.java` file).

## Agent rules

### Do

1. Verify which build system a change belongs to (root reactor vs. standalone `signup-with-plugins`/`api-test`/`ui-test`) before writing build commands.
2. Read the linked module guide before editing inside `signup-service`, `signup-ui`, `signup-with-plugins`, or `deploy`.
3. Keep secrets out of commits — use the `.env.local` / `application-local.properties` local-override pattern already established, not real values in tracked files.
4. Update `docs/esignet-signup-openapi.yaml` when changing a REST API's request/response contract.
5. Preserve `-D` system property ordering: in a Maven command, `-D` flags go before the goal (e.g. `mvn -Dfoo=bar test`, never after); in a plain `java` command, `-D` flags go before `-jar` (e.g. `java -Dfoo=bar -jar target/x.jar`).

### Do not

1. Do not assume `mvn clean install` at the repo root builds `signup-with-plugins`, `api-test`, or `ui-test` — it does not; they are separate reactors.
2. Do not duplicate `api-test/CLAUDE.md` content here — link to it instead, and keep both in sync if API-test conventions change.
3. Do not hand-edit Helm `values.yaml` files to inject secrets; follow the `--set`/`kubectl create secret` pattern used by the scripts in `deploy/`.
4. Do not remove the `mock-auth-data/` folder from either Helm chart under the assumption it's redundant — both charts consume their own copy independently.
