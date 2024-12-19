## Overview

This is the docker-compose setup to run eSignet Signup service with mock identity system. This is not for production use.

## Run signup service in local with all its dependencies

1. Run `docker compose --file dependent-docker-compose.yml up` to start all the dependent services.
2. Go to command line for the project root directory and run `mvn clean install -Dgpg.skip=true -DskipTests=true`
3. Add [esignet-mock-plugin.jar](../signup-service/target/signup-plugins/esignet-mock-plugin.jar) to signup-service classpath in your IDE.
4. Add [kernel-auth-adapter-lite.jar](../signup-service/target/signup-plugins/kernel-auth-adapter-lite.jar) to signup-service classpath in your IDE.
5. Update below properties in [application-local.properties](../signup-service/src/main/resources/application-local.properties) with valid values:

       mosip.internal.domain.url=https://api-internal.dev.mosip.net

       keycloak.external.url=https://iam.dev.mosip.net

       mosip.signup.client.secret=secret

6. Start the [SignUpServiceApplication.java](../signup-service/src/main/java/io/mosip/signup/SignUpServiceApplication.java) from your IDE.
7. Access the service swagger with this URL - http://localhost:8089/v1/signup/swagger-ui.html
8. Import files under [postman-collection](../postman-collection) folder into your postman to test/validate registration flow.


## Prerequisite to run Identity verification flow from postman collection

1. Onboard signup-service as a OIDC client in esignet-service:

Execute [create-signup-oidc-keystore.sh](../docs/create-signup-oidc-keystore.sh) to generate a keypair. This script after
successful execution creates 2 files in the project root directory:

* oidckeystore.p12
* public_key.jwk

As esignet only supports confidential OIDC clients, we should generate a RSA keypair to onboard signup-service. RSA private key is
stored in the oidckeystore.p12 file and the corresponding public key is written to public_key.jwk file. 

Copy the public key in public_key.jwk file and update the same in the `Register Signup OIDC/Create Signup OIDC client` request body.

Run `Register Signup OIDC/Create Signup OIDC client` in postman before starting the identity verification flow.