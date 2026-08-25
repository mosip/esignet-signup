# AGENTS.md — signup-service

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

Spring Boot backend for the signup module. Exposes REST endpoints to generate/verify OTP challenges, register a user, check registration status, reset a password, and run the WebSocket-based online video identity-verification (KYC) flow. It is built together with the sibling module `signup-integration-api` (the SPI/DTO library that ID-registry plugins implement), which lives at `../signup-integration-api` and is part of the same root Maven reactor.

## Layout

- `src/main/java/io/mosip/signup/controllers` — `SignUpController`, `RegistrationController`, `ResetPasswordController`, `IdentityVerificationController`, `WebSocketController`, `CsrfController`.
- `src/main/java/io/mosip/signup/config` — Spring config: `Config`, `SecurityConfig`, `WebSocketConfig`, Kafka producer/consumer config, exception advice.
- `src/main/java/io/mosip/signup/dto` — request/response DTOs, including the identity-verification transaction models.
- `src/main/resources/application-default.properties` — default runtime configuration (cache TTLs, password policy, supported languages, etc.).
- `src/main/resources/application-local.properties` — local-development override template with placeholder secret values (`mosip.signup.client.secret=secret-from-env`, empty `spring.data.redis.password`, a default local `mosip.signup.oauth.keystore-password`).
- `configure_start.sh` — Docker entrypoint script; copies the configured plugin JAR (`plugin_name_env`, optionally downloaded from `signup_plugin_url_env`) onto the classpath before starting the app.
- `Dockerfile` — builds the runtime image; the service listens on port `8089` (`EXPOSE 8089`).

## How to run

Build (JDK 21 required):

```bash
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

Run locally from an IDE (per `README.md`):

1. Add the plugin jars produced under `target/signup-plugins/` (e.g. `kernel-auth-adapter-lite.jar`, `esignet-mock-plugin.jar`) to the module's external libraries.
2. Update `src/main/resources/application-local.properties`:
   - `MOSIP_API_INTERNAL_HOST` — a real MOSIP (LTS) environment base URL.
   - `keycloak.external.url` — the environment's Keycloak URL.
   - `mosip.signup.client.secret` — the real client secret for the signup-service OIDC client registered in Keycloak.
3. Run `SignUpServiceApplication.main()`.

The service requires the MOSIP kernel `otpmanager`, `authmanager`, `auditmanager`, and `notifier` modules to be reachable — there is no fully offline mode for the live-registry flow (the Mock Identity System plugin avoids needing a live ID registry specifically, but kernel dependencies still apply).

## Caching model

Registration, reset-password, and identity-verification flows each use named Spring caches (e.g. `challenge_generated`, `challenge_verified`, `status_check`, `identity_verification`, `slot_allotted`, `verified_slot`, `slots_connected`) with specific populate/evict points per endpoint. The full table is in `README.md` in this directory — read it before changing an endpoint's cache interaction, since evicting the wrong key breaks the next step in the flow.

## Agent rules

### Do

1. Read the caching table in `signup-service/README.md` before touching any controller that reads/writes a Spring cache.
2. Keep new configuration properties documented with a sensible default in `application-default.properties`.
3. Treat `application-local.properties` as a template — its committed values are placeholders, not real secrets.

### Do not

1. Do not commit real values for `mosip.signup.client.secret`, `spring.data.redis.password`, or `mosip.signup.oauth.keystore-password` in `application-local.properties`.
2. Do not change the port `signup-service` exposes (`8089` per `Dockerfile`) without also updating the Helm chart (`../helm/signup-service`) and `deploy/signup-service` scripts.
