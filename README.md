# eSignet Signup

signup-service is part of the esignet, but has a separate Helm chart to install and manage it in a completely independent namespace.

## Installing in k8s cluster using helm
### Pre-requisites
1. Set the kube config file of the Esignet k8 cluster having esignet services is set correctly in PC.
2. Below are the dependent services required for signup service integrated with [Mock Identity System](https://github.com/mosip/esignet-mock-services/tree/master/mock-identity-system)
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
#### Prerequisites for MOSIP kernel services:
  * Artifactory service [clone this repo and run install.sh under deploy directory](https://github.com/mosip/artifactory-ref-impl/tree/release-1.3.x/deploy#install)
  * Config server [clone this repo and run install.sh under deploy directory](https://github.com/mosip/mosip-infra/tree/master/deployment/v3/mosip/config-server#install)
  * Initialize the db script to create mosip_kernel and mosip_audit databases make sure to update the existing db-common-secret in init_values.yaml
    * copy db-common-secret from existing postgres deployment secret
    * run the postgres-init.sh
      ```
        cd deploy
        ./postgres-init.sh
      ```
#### Please follow the steps below to install the specified services using the [install.sh](https://github.com/mosip/mosip-infra/tree/develop/deployment/v3/mosip/kernel#install)
* Open the install.sh script for editing.
  * Comment out all services in the script except for the following:
    * otpmanager
    * authmanager
    * auditmanager
    * notifier
    Ensure only the mentioned services remain active in the script before proceeding with the installation.

## Partner onboarding
* Perform Partner onboarding for esignet Signup OIDC client using [steps](partner-onboarder/README.md) only if mosip-identity plugin is used.  

## APIs
API documentation is available [here](https://mosip.stoplight.io/docs/identity-provider/branches/signupV1/t9tvfbteqqokf-e-signet-signup-portal-ap-is).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
