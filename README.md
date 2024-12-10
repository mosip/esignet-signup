# eSignet Signup

signup-service is part of the esignet, but has a separate Helm chart to install and manage it in a completely independent namespace.

## Installing in k8s cluster using helm
### Pre-requisites
1. Set the kube config file of the Esignet k8 cluster having esignet services is set correctly in PC.
1. Below are the dependent services required for signup service integrated with MOSIP IDA:
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
## Deploy dependent services for Signup onboarding
1. msg-gateway
2. config-server
3. artifactory
4. mock-smtp
5. kernel
# Partner Onboarder
## Install
* Create a directory for onboarder on the NFS server at `/srv/nfs/<sandbox>/onboarder/`:
```
mkdir -p /srv/nfs/mosip/<sandbox>/onboarder/
```
* Ensure the directory has 777 permissions:
```
chmod 777 /srv/nfs/mosip/<sandbox>/onboarder
```
* Add the following entry to the /etc/exports file:
```
/srv/nfs/mosip/<sandbox>/onboarder *(ro,sync,no_root_squash,no_all_squash,insecure,subtree_check)
```
* Set `values.yaml` to run onboarder for specific modules.
* run `./install.sh`.
```
./install.sh
```
* Perform Partner onboarding for esignet Signup OIDC client using [steps](partner-onboarder/README.md) only if mosip-identity plugin is used.  

## APIs
API documentation is available [here](https://mosip.stoplight.io/docs/identity-provider/branches/signupV1/t9tvfbteqqokf-e-signet-signup-portal-ap-is).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
