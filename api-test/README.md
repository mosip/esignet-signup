# ESignet Signup API Test Rig

## Overview

The **ESignet Signup API Test Rig** is designed for the execution of module-wise automation API tests for the esignet signup services. This test rig utilizes **Java REST Assured** and **TestNG** frameworks to automate testing of the esignet signup service API functionalities. The key focus is to validate the Register user, Reset password, Identity verification and related functionalities provided by the esignet signup service module.

---

## Test Categories

- **Smoke**: Contains only positive test scenarios for quick verification.
- **Regression**: Includes all test scenarios, covering both positive and negative cases.

---

## Coverage

This test rig covers only **external API endpoints** exposed by the esignet signup services module.

---

## Pre-requisites

Before running the automation tests, ensure the following software is installed on the machine:

- **Java 21** ([download here](https://jdk.java.net/))
- **Maven 3.9.6** or higher ([installation guide](https://maven.apache.org/install.html))
- **Lombok** (Refer to [Lombok Project](https://projectlombok.org/))
- **setting.xml** ([download here](https://github.com/mosip/mosip-functional-tests/blob/master/settings.xml))
- **apitest-commons** library should be cloned and the JAR should be built. Refer to ([README](https://github.com/mosip/mosip-functional-tests/blob/release-1.3.0/apitest-commons/README.md))

### For Windows

- **Git Bash 2.18.0** or higher
- Ensure the `settings.xml` file is present in the `.m2` folder.

### For Linux

- The `settings.xml` file should be present in two places:
  - In the regular Maven configuration folder (`/conf`)
  - Under `/usr/local/maven/conf/`

---

# API Test Rig Signup Configuration

The API Test Rig is configured to work with different core service plugins, such as MOSIP-ID and Mock-Identity-System. It allows for dynamic testing and validation of workflows related to identity management and authentication, specifically for the signup process.

## Integration of eSignet-signup API Test Rig with Core Service Plugins

The Test Rig is dynamically configured based on the core service plugin being tested (either MOSIP-ID or Mock-Identity-System). The configuration is as follows:

### 1. Configuration for MOSIP-ID Plugin:
- **eSignetbaseurl**: The Test Rig will use the live eSignet instance integrated with the MOSIP-ID service.
- **signupBaseUrl**: The Test Rig will use the live Signup instance integrated with the MOSIP-ID service.
- **schemaVersion**: Specifies the schema version in case the environment has multiple schemas (e.g., "v1.0", "v2.0", etc.).
- **mosip_components_base_urls**: A string defining the base URLs for various components.
- **esignetActuatorPropertySection**: To fetch the configuration and properties from the actuator for service interactions.

### 2. Configuration for Mock-Identity-System Plugin:
- **eSignetbaseurl**: The Test Rig will use the live eSignet instance integrated with the Mock-Identity-service.
- **signupBaseUrl**: The Test Rig will use the live Signup instance integrated with the Mock-Identity-service.
- **usePreConfiguredOtp**: A flag to use pre-configured OTPs. Set to "true" for OTP-based workflows.
- **esignetActuatorPropertySection**: To fetch the configuration and properties from the actuator for service interactions.


### Deployment Configuration (Required for API Test Rig):
These configurations need to be added as part of the eSignet service deployment to support the API Test Rig:

- **MOSIP_ESIGNET_AUTHENTICATE_ATTEMPTS**: 300
- **MOSIP_ESIGNET_SEND_OTP_ATTEMPTS**: 300
- **MOSIP_ESIGNET_AUTH_CHALLENGE_BIO_MAX_LENGTH**: 200000
- **MOSIP_ESIGNET_PREAUTHENTICATION_EXPIRE_IN_SECS**: 600
- **MOSIP_ESIGNET_CAPTCHA_REQUIRED**: (empty)

These configurations need to be added as part of the Signup service deployment to support the API Test Rig:

- **MOSIP_SIGNUP_SEND_CHALLENGE_CAPTCHA_REQUIRED**: false


These parameters must be included in the eSignet and signup deployment YAML for the API Test Rig to function correctly, independent of which plugin is being used.

## Access Test Automation Code

You can access the test automation code using either of the following methods:

### From Browser

1. Clone or download the repository as a zip file from [GitHub](https://github.com/mosip/esignet-signup).
2. Unzip the contents to your local machine.
3. Open a terminal (Linux) or command prompt (Windows) and continue with the following steps.

### From Git Bash

1. Copy the Git repository URL: `https://github.com/mosip/esignet-signup`
2. Open **Git Bash** on your local machine.
3. Run the following command to clone the repository:
   ```sh
   git clone https://github.com/mosip/esignet-signup
   ```

---

## Build Test Automation Code

Once the repository is cloned or downloaded, follow these steps to build and install the test automation code:

1. Navigate to the project directory:
   ```sh
   cd api-test
   ```

2. Build the project using Maven:
   ```sh
   mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
   ```

This will download the required dependencies and prepare the test suite for execution.

---

## Execute Test Automation Suite

You can execute the test automation code using either of the following methods:

### Using Jar

To execute the tests using Jar, use the following steps:

1. Navigate to the `target` directory where the JAR file is generated:
   ```sh
   cd target/
   ```

2. Run the automation test suite JAR file:
   ```
   java -jar -Dmodules=signup -Denv.user=api-internal.<env_name> -Denv.endpoint=<base_env> -Denv.testLevel=smokeAndRegression -jar apitest-signup-1.2.1-jar-with-dependencies.jar
   ```
   
# Using Eclipse IDE

To execute the tests using Eclipse IDE, use the following steps:

## 1. **Install Eclipse (Latest Version)**
   - Download and install the latest version of Eclipse IDE from the [Eclipse Downloads](https://www.eclipse.org/downloads/).

## 2. **Import the Maven Project**

   After Eclipse is installed, follow these steps to import the Maven project:

   - Open Eclipse IDE.
   - Go to `File` > `Import`.
   - In the **Import** wizard, select `Maven` > `Existing Maven Projects`, then click **Next**.
   - Browse to the location where the `api-test` folder is saved (either from the cloned Git repository or downloaded zip).
   - Select the folder, and Eclipse will automatically detect the Maven project. Click **Finish** to import the project.

## 3. **Build the Project**

   - Right-click on the project in the **Project Explorer** and select `Maven` > `Update Project`.
   - This will download the required dependencies as defined in the `pom.xml` and ensure everything is correctly set up.

## 4. **Run the Tests**

   To execute the test automation suite, you need to configure the run parameters in Eclipse:

   - Go to `Run` > `Run Configurations`.
   - In the **Run Configurations** window, create a new configuration for your tests:
     - Right-click on **Java Application** and select **New**.
     - In the **Main** tab, select the project by browsing the location where the `api-test` folder is saved, and select the **Main class** as `io.mosip.testrig.apirig.signup.testrunner.MosipTestRunner`.
   - In the **Arguments** tab, add the necessary **VM arguments**:
     - **VM Arguments**:
       ```
       -Dmodules=signup -Denv.user=api-internal.<env_name> -Denv.endpoint=<base_env> -Denv.testLevel=smokeAndRegression```

## 5. **Run the Configuration**

   - Once the configuration is set up, click **Run** to execute the test suite.
   - The tests will run, and the results will be shown in the **Console** tab of Eclipse.

   **Note**: You can also run in **Debug Mode** to troubleshoot issues by setting breakpoints in your code and choosing `Debug` instead of `Run`.

---

## 6. **View Test Results**

   - After the tests are executed, you can view the detailed results in the `api-test\testng-report` directory.
   - The report will have two sections:
       - One section for pre-requisite APIs test cases.
       - Another section for core test cases.

---

## Details of Arguments Used

- **env.user**: Replace `<env_name>` with the appropriate environment name (e.g., `dev`, `qa`, etc.).
- **env.endpoint**: The environment where the application under test is deployed. Replace `<base_env>` with the correct base URL for the environment (e.g., `https://api-internal.<env_name>.mosip.net`).
- **env.testLevel**: Set this to `smoke` to run only smoke test cases, or `smokeAndRegression` to run both smoke and regression tests.
- **jar**: Specify the name of the JAR file to execute. The version will change according to the development code version. For example, the current version may look like `apitest-signup-1.2.1-jar-with-dependencies.jar`.

### Build and Run Info

To run the tests for both **Smoke** and **Regression**:

1. Ensure the correct environment and test level parameters are set.
2. Execute the tests as shown in the command above to validate esignet signup services API functionalities.

---

## License

This project is licensed under the terms of the [Mozilla Public License 2.0](https://github.com/mosip/mosip-platform/blob/master/LICENSE)
