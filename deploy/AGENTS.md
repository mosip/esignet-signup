# AGENTS.md — deploy

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

Shell scripts that install, restart, and delete the signup module (`signup-service`, `signup-ui`, and their plugin variants) on a Kubernetes cluster, plus the shared prerequisites (Keycloak init, Redis/Kafka config copy, kernel service dependencies). These scripts drive the Helm charts under `../helm`.

## Layout

- `prereq.sh` — copies configmaps/secrets (`esignet-global`, `redis-config`, `redis` secret) from existing `esignet`/`redis` namespaces into the `signup` namespace, runs `keycloak/keycloak-init.sh`, and interactively prompts for reCAPTCHA site/secret keys (stored via `kubectl create secret generic signup-captcha --from-literal=...`, not written to a values file).
- `install-signup.sh` — top-level installer; prompts for a plugin choice (MOSIP ID vs. Mock), then runs `signup-with-plugins/<chosen-plugin>/install.sh` followed by `signup-ui/install.sh`.
- `delete-signup.sh`, `restart-signup.sh` — corresponding teardown/restart entry points.
- `postgres-init.sh` — initializes `mosip_kernel`/`mosip_audit` databases; requires the `db-common-secret` from an existing Postgres deployment.
- `copy_cm_func.sh` — shared helper used by `prereq.sh` to copy a configmap or secret between namespaces.
- Per-component subfolders, each with its own `install.sh`/`delete.sh`/`restart.sh` (and `README.md` where present):
  - `artifactory/`, `config-server/`, `kernel/`, `mock-smtp/`, `msg-gateway/` — MOSIP kernel prerequisites.
  - `keycloak/` — `keycloak-init.sh` + `keycloak-init-values.yaml`.
  - `signup-service/`, `signup-ui/` — install the two main Helm charts (each with its own `values.yaml`).
  - `signup-with-plugins/signup-with-mock-plugin/`, `signup-with-plugins/signup-with-mosipid-plugin/` — plugin-specific install flows (see `../signup-with-plugins/AGENTS.md`); each script does an interactive y/n confirmation, prompts whether a Prometheus ServiceMonitor operator is deployed, and installs via `helm -n signup install ... --version <CHART_VERSION> --set ...`.
  - `signup-apitestrig/`, `signup-uitestrig/` — install the test-rig components into the cluster.
  - `reporting/signup.ndjson` — Kibana/reporting index-pattern seed data.

## How secrets actually flow (verified against the scripts)

Secrets are **not** hand-edited into `values.yaml`. The scripts either:

1. Copy an existing Kubernetes `Secret`/`ConfigMap` from another namespace via `copy_cm_func.sh`, which fetches the source resource as YAML, rewrites its namespace/name, and pipes it to `kubectl create -f -` (see `prereq.sh`'s use of `copy_cm_func.sh` for `redis`/`esignet-global`). To create a brand-new secret from literal values (not a copy), use `kubectl create secret ... --from-literal=... --dry-run=client -o yaml | kubectl apply -f -` (see `prereq.sh`'s captcha/keystore secret creation), or
2. Pass values at install time with Helm's `--set` (see `deploy/signup-service/install.sh`'s `--set image.repository=... --set pluginNameEnv=... --set pluginUrlEnv=...`).

Follow this same pattern for any new secret-bearing configuration rather than adding it as a plaintext default in a chart's `values.yaml`.

## How to run

Typical order (per root `README.md` and `install-signup.sh`):

```bash
cd deploy
./prereq.sh
./install-signup.sh
```

To point at a non-default kubeconfig, pass it as the first argument, e.g. `./install-signup.sh /path/to/kubeconfig`.

## Agent rules

### Do

1. Keep new install scripts idempotent where the existing ones are (`kubectl create ns $NS || true` pattern).
2. Pass secrets via `--set` or `kubectl create secret` as the existing scripts do.
3. Update the matching `README.md` (root or `helm/<chart>/README.md`) when changing an install script's prompts or required environment variables.

### Do not

1. Do not add real secret values to any `values.yaml` in `../helm` or under `deploy/*/values.yaml`.
2. Do not assume these scripts run non-interactively — most prompt with `read -p`; if adding CI automation around them, pipe answers explicitly rather than assuming defaults.
