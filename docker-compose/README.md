## Overview

This is the docker-compose setup to run eSignet Signup service with mock identity system. This is not for production use.

## Run signup service in local with all its dependencies

1. Run `docker compose --file dependent-docker-compose.yml up` to start all the dependent services.
2. Go to [signup-with-plugins](../signup-with-plugins) folder and run `mvn clean install -Dgpg.skip=true` from the command line.
3. Add [esignet-mock-plugin.jar](../signup-with-plugins/target/esignet-mock-plugin.jar) to signup-service classpath in your IDE.
4. Update below properties in [application-local.properties](../signup-service/src/main/resources/application-local.properties) with valid values:

       mosip.internal.domain.url=https://api-internal.dev.mosip.net

       keycloak.external.url=https://iam.dev.mosip.net

       mosip.signup.client.secret=secret

6. Start the [SignUpServiceApplication.java](../signup-service/src/main/java/io/mosip/signup/SignUpServiceApplication.java) from your IDE.
7. Access the service swagger with this URL - http://localhost:8089/v1/signup/swagger-ui.html
8. Import files under [postman-collection](../postman-collection) folder into your postman to test/validate registration flow.