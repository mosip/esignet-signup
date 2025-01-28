# eSignet Signup

## Overview

This repository contains a signup UI and signup service to handle signup operations by the end user. This module can be
used to fast-track the availability of any digital service to end users via eSignet. eSignet has built-in support for the
integration with the signup module. The signup service is flexible to connect to any ID registry system via a well-defined plugin interface.

Currently, signup supports below features:
1. Register User
2. Reset password
3. Online video based identity verification workflow integration via plugin

## Build (for developers)
The project requires JDK 11.
1. Build:
    ```
    $ mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
    ```

## Installing in k8s cluster using helm

signup-service is part of the esignet, but has a separate Helm chart to install and manage it in a completely independent namespace.

### Pre-requisites
1. Set the kube config file of the Esignet k8 cluster having esignet services is set correctly in PC.
1. Below are the dependent services required for signup service integrated with [Mock Identity System](https://github.com/mosip/esignet-mock-services/tree/master/mock-identity-system)
   | Chart | Chart version |
   |---|---|
   |[Keycloak](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/iam) | 7.1.18 |
   |[Keycloak-init](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/iam) | 12.0.1-B3 |
   |[Kafka](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/kafka) | 0.4.2 |

### Setup pre-requisites for signup services
```
cd deploy
./prereq.sh
```
### Install Signup service
* Install `kubectl` and `helm` utilities.
* Run `install-signup.sh` to deploy signup services.
  ```
  cd deploy
  ./install-signup.sh
  ```
### Delete
* Run `delete-signup.sh` to remove signup services.
  ```
  cd deploy
  ./delete-signup.sh
  ```
### Restart
* Run `restart-signup.sh` to restart signup services.
  ```
  cd deploy
  ./restart-signup.sh
  ```
### Additional services required
To complete the signup portal deployment below MOSIP kernel services are required to be deployed.
* otpmanager
* authmanager
* auditmanager
* notifier

* Initialize the db script to create mosip_kernel and mosip_audit databases make sure to update the existing db-common-secret in init_values.yaml if postgres-initialization already done
  * copy db-common-secret from existing postgres deployment secret if its already created
  * run the postgres-init.sh
  ```
    cd deploy
    ./postgres-init.sh
  ```
#### Prerequisites for MOSIP kernel services:
1. msg-gateway
2. config-server
3. artifactory
4. mock-smtp
5. kernel
```
  cd deploy (follow the above sequence and run the install.sh for each module installation)
  
```  
## Partner onboarding
* Partner onboarding for esignet Signup OIDC client with mock can be performed manually with below steps
* Download and import eSignet-with-mock.postman_environment.json and eSignet.postman_collection.json postman collection from [here](../postman-collection) 

Update the "client_secret" and iam_url(keycoak) in the request body.

Run the requests under

# OIDC Client Management Instructions

1. Navigate to **"OIDC Client Mgmt"** -> **"Mock"** -> **"Get Auth token"** to fetch the authentication token.

2. Navigate to **"OIDC Client Mgmt"** -> **"Mock"** -> **"Get CSRF token"** to fetch the CSRF token.

3. **Before executing the "Create OIDC client" request**, update the following fields in the request:
   - `url`
   - `logo-uri`
   - `redirect-uri`
   - `client-name`
   - `client-id`

4. Store the private key of the private-public key pair in `.p12` format and:
   - Mount it as a `signup-keystore` secret to the **signup deployment**.
   - Make sure to update the `signup-keystore-password` in the secrets as passed while creating the p12 file.

5. Update the public key in the **"Create OIDC client"** request in **JWK format**.

6. Navigate to **"OIDC Client Mgmt"** -> **"Mock"** -> **"Create OIDC client"** to execute the request.


## APIs
API documentation is available [here](docs/esignet-signup-openapi.yaml).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
