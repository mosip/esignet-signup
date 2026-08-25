# AGENTS.md — signup-with-plugins

Parent guide: [../AGENTS.md](../AGENTS.md)

## Purpose

Docker build assets that bundle the `signup-service` base image with a chosen identity-verification/profile-registry plugin JAR preloaded, so the resulting image starts up already configured for a specific ID registry. All plugin implementations referenced here (`esignet-mock-plugin.jar`, `mosip-identity-plugin.jar`) come from the separate [`mosip/esignet-plugins`](https://github.com/mosip/esignet-plugins) repository and are downloaded/copied as prebuilt JARs — this directory does not contain plugin source code.

## Layout

- `pom.xml` — its own Maven reactor, parented to the repo's `signup-parent` POM but **not** listed in the root `pom.xml`'s `<modules>`, so it is built independently of `signup-service`/`signup-integration-api`.
- `Dockerfile` — builds the combined image, layered on the `signup-service` base image.
- `README.md` — explains the plugin-loading mechanism: at runtime the `plugin_name_env` environment variable names the JAR (e.g. `esignet-mock-plugin.jar`) to copy from the mounted/downloaded plugins directory into the signup-service classpath before the service starts.

Two deployment flavors exist under `../deploy/signup-with-plugins/`:

- `signup-with-mock-plugin/` — installs with the Mock Identity System plugin (`esignet-mock-plugin.jar`), for demo/dev environments.
- `signup-with-mosipid-plugin/` — installs with the MOSIP ID plugin, for real MOSIP-ID-backed environments.

## How to run

Build (JDK 21 required, from this directory since it is not part of the root reactor):

```bash
cd signup-with-plugins
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

CI (`.github/workflows/chart-lint-publish.yml`'s sibling `push-trigger.yml`) builds this module with `SERVICE_LOCATION: ./signup-with-plugins` as a separate job from the root reactor build, and only builds/publishes its Docker image after the corresponding Nexus snapshot publish succeeds and the parent POM version is still a `-SNAPSHOT`.

## Agent rules

### Do

1. Treat this module's `pom.xml` as an independent build — do not assume the root `mvn clean install` covers it.
2. Keep the plugin JAR filenames referenced here (`esignet-mock-plugin.fileName`, `mosip-identity-plugin.fileName` properties in `pom.xml`) in sync with what `mosip/esignet-plugins` actually publishes.

### Do not

1. Do not add plugin implementation source code directly into this directory — plugins live in `mosip/esignet-plugins` and are consumed as built artifacts here.
2. Do not assume both `signup-with-mock-plugin` and `signup-with-mosipid-plugin` (see `../deploy/signup-with-plugins/`) share identical configuration — each has its own `install.sh`/`values.yaml`.
