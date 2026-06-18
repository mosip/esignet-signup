## Overview

This is the docker-compose setup to run eSignet Signup service with mock identity system. This is not for production use.

## Prerequisites

1. JDK 21
2. Maven
3. Docker
4. Git bash
5. Postman

## Bring up the complete signup setup for a demo

The [docker-compose.yml](docker-compose.yml) file brings up the complete signup setup — `signup-service` and `signup-ui` along with all the dependent services. With this setup, the signup service need not be run from the IDE.

### Step 1: Generate the signup OIDC keystore

1. Open a Git bash terminal in the [postman-collection](../postman-collection) folder and run:

   ```bash
   ./create-signup-oidc-keystore.sh
   ```

   This generates `oidckeystore.p12` and `public_key.jwk` in the repository root. The keystore is mounted into the `signup-service` container by `docker-compose.yml`, and `public_key.jwk` is needed while onboarding the signup OIDC client in [Step 4](#step-4-onboard-the-signup-oidc-client).

### Step 2: Configure environment-specific values

Update the below environment variables of the `signup-service` in `docker-compose.yml` with valid values. They are required for OTP generation and notifications:

   ```properties
   MOSIP_API_INTERNAL_HOST=https://api-internal.<env-name>.mosip.net
   KEYCLOAK_EXTERNAL_URL=https://iam.<env-name>.mosip.net
   MOSIP_SIGNUP_CLIENT_SECRET=<secret-from-env>
   ```

   > **Note:** `MOSIP_ESIGNET_SIGNUP_ID_TOKEN_AUDIENCE` is already set on the `esignet` service to `mosip-signup-oauth-client`, which is the client ID created by the signup Postman collection in [Step 4](#step-4-onboard-the-signup-oidc-client). If you onboard the signup OIDC client with a different client ID, update this variable accordingly.

### Step 3: Start the services

Open a terminal in the current directory and run:

   ```bash
   docker compose up
   ```

Wait until all the services are up:

| Service | URL |
|---|---|
| signup UI | [http://localhost:3001](http://localhost:3001) |
| signup service Swagger | [http://localhost:8089/v1/signup/swagger-ui.html](http://localhost:8089/v1/signup/swagger-ui.html) |
| eSignet UI | [http://localhost:3000](http://localhost:3000) |
| eSignet service Swagger | [http://localhost:8088/v1/esignet/swagger-ui.html](http://localhost:8088/v1/esignet/swagger-ui.html) |

### Step 4: Onboard the signup OIDC client

1. Import the files located in the [postman-collection](../postman-collection) folder into Postman and follow the [Postman README](../postman-collection/README.md) to create the signup OIDC client. Use the `public_key.jwk` generated in [Step 1](#step-1-generate-the-signup-oidc-keystore) as the client public key.

2. To create an eSignet client and a mock identity, refer to the [eSignet Docker Compose documentation](https://github.com/mosip/esignet/blob/master/docker-compose/README.md#how-to-bring-up-the-complete-esignet-setup-for-a-demo).

### Step 5: Access the signup UI

Open [http://localhost:3001](http://localhost:3001) in the browser and walk through the signup flow.

## Run signup service in local with all its dependencies

### Step 1: Configure Dependent Services

1. Open the `dependent-docker-compose.yml` file.
2. Under the `eSignet` environment section, add the following property:

   ```
   MOSIP_ESIGNET_SIGNUP_ID_TOKEN_AUDIENCE= <your-signup-client-id>;
   ```

   > **Note:** `<your-signup-client-id>` is the client ID for the signup application.  
   > You can use any ID here, but make sure to create the signup client as explained in [Step 5](#step-5-configure-the-esignet-signup-client).

3. Open a terminal or command prompt in the current directory and run the following command.

   ```
   docker compose --file dependent-docker-compose.yml up
   ```

This will start all dependent services required for eSignet Signup.

### Step 2: Configure eSignet Core

For instructions on setting up eSignet Core, refer to the [eSignet Docker Compose documentation](https://github.com/mosip/esignet/blob/master/docker-compose/README.md#how-to-bring-up-the-complete-esignet-setup-for-a-demo). Upon completing these steps, you should have successfully created both an eSignet client and a mock identity.

### Step 3: Configuration for Running eSignet Signup

1. Return to the `esignet-signup/docker-compose` documentation.

2. Update below properties in [application-local.properties](../signup-service/src/main/resources/application-local.properties) with valid values:

   ```
   MOSIP_API_INTERNAL_HOST=https://api-internal.<env-name>.mosip.net

   keycloak.external.url=https://iam.<env-name>.mosip.net

   mosip.signup.client.secret=<secret-from-env>

   mosip.signup.oauth.keystore-path=../../oidckeystore.p12
   ```

3. Go to the [signup-with-plugins](../signup-with-plugins) folder and run the following command to build the plugins.

   ```
   mvn clean install -Dgpg.skip=true
   ```

4. Go to [signup-service](../signup-service) folder and run the following command.

   ```
   mvn clean install -Dgpg.skip=true -DskipTests=true
   ```

5. Start the eSignet signup-service with the below command. `<plugin-path>` should be replaced with the absolute path to the plugin jar built and saved under [signup-with-plugins/target folder](../signup-with-plugins/target)

   ```c
   // change directory to target
   cd target
   // running the signup-service jar file, with external plugin
   java -Dfile.encoding=UTF-8 -Dloader.path=<plugin-path> -jar <signup-service jar file name>
   ```

6. Access the service Swagger at:  
   [http://localhost:8089/v1/signup/swagger-ui.html](http://localhost:8089/v1/signup/swagger-ui.html)

### Step 4: Testing with Postman

1. Import the files located in the [postman-collection](../postman-collection) folder into Postman.

2. Before starting, refer to the [Postman README](../postman-collection/README.md) for detailed instructions.

### Step 5: Configure the eSignet Signup Client

1. Navigate to the [esignet-signup documentation](https://github.com/mosip/esignet-signup/blob/master/README.md#partner-onboarding) to create a signup client.

2. Import the Postman collection and its environment file (available in the cloned repository) into the Postman application.

3. Follow the OIDC client management instructions in the esignet-signup documentation. You will need the following details to generate an auth token:

   ```
   iam_url = https://iam.<env-name>.mosip.net
   client_id = <your-signup-client-id>
   client_secret = <your-signup-client-secret>
   ```

4. Run the signup UI (React application) using the following command. This will start the frontend application required for the signup flow.

   ```
   npm start
   ```
