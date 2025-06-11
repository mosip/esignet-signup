## Overview

This is the docker-compose setup to run eSignet Signup service with mock identity system. This is not for production use.

## Prerequisites

1. Java 11
2. Maven
3. Docker
4. Git bash
5. Postman

## Run signup service in local with all its dependencies

1. Open terminal/command prompt for the current directory and run `docker compose --file dependent-docker-compose.yml up` to start all the dependent services.
2. Go to [signup-with-plugins](../signup-with-plugins) folder and run `mvn clean install -Dgpg.skip=true` from the command line to build the plugins.
3. Update below properties in [application-local.properties](../signup-service/src/main/resources/application-local.properties) with valid values:

       mosip.internal.domain.url=https://api-internal.<env-name>.mosip.net

       keycloak.external.url=https://iam.<env-name>.mosip.net

       mosip.signup.client.secret=<secret-from-env>

       mosip.signup.oauth.keystore-path=../../oidckeystore.p12

5. Go to [signup-service](../signup-service) folder and run `mvn clean install -Dgpg.skip=true -DskipTests=true` from command prompt.
6. Start the eSignet signup-service with the below command. `<plugin-path>` should be replaced with the absolute path to the plugin jar built and saved under [signup-with-plugins/target folder](../signup-with-plugins/target)
   ```c
   // change directory to target
   cd target
   // running the signup-service jar file, with external plugin
   java -Dloader.path=<plugin-path> -jar <signup-service jar file name>
   ```

7. Access the service swagger with this URL - http://localhost:8089/v1/signup/swagger-ui.html
8. Import files under [postman-collection](../postman-collection) folder into your postman to test/validate registration flow. Kindly refer [README.md](../postman-collection/README.md) before starting with the postman collection.