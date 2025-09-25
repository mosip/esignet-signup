## Overview

This is the docker-compose setup to run eSignet Signup service with mock identity system. This is not for production use.

## Prerequisites

1. Java 11
2. Maven
3. Docker
4. Git bash
5. Postman

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
   mosip.internal.domain.url=https://api-internal.<env-name>.mosip.net

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
   java -Dloader.path=<plugin-path> -jar <signup-service jar file name>
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
