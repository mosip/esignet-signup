# esignet-signup

signup-service is part of the esignet modules, but has a separate Helm chart so as to install and manage it in a completely independent namespace.

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 11.
1. Build and install:
    ```
    $ mvn clean install -Dgpg.skip=true
    ```
1. Build Docker for a service:
    ```
    $ docker build -f Dockerfile
    ```

## Installing in k8s cluster using helm
### Pre-requisites
1. Set the kube config file of the Mosip cluster having dependent services is set correctly in PC.
1. Make sure [DB setup](db_scripts/README.md#install-in-existing-mosip-k8-cluster) is done.
1. Add / merge below mentioned properties files into existing config branch:
    * [signup-default.properties](https://github.com/mosip/mosip-config/blob/v1.2.0.1-B3/esignet-default.properties)
    * [application-default.properties](https://github.com/mosip/mosip-config/blob/v1.2.0.1-B3/application-default.properties)
1. Below are the dependent services required for signup service integrated with MOSIP IDA:
   | Chart | Chart version |
   |---|---|
   |[Keycloak](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/iam) | 7.1.18 |
   |[Keycloak-init](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/iam) | 12.0.1-B3 |
   |[Postgres](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/postgres) | 10.16.2 |
   |[Postgres Init](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/postgres) | 12.0.1-B3 |
   |[Minio](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/object-store) | 10.1.6 |
   |[Kafka](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/external/kafka) | 0.4.2 |
   |[Config-server](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/mosip/config-server) | 12.0.1-B3 |
   |[Websub](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/mosip/websub) | 12.0.1-B2 |
   |[Artifactory server](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/mosip/artifactory) | 12.0.1-B3 |
   |[Keymanager service](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B3/deployment/v3/mosip/keymanager) | 12.0.1-B2 |
   |[Kernel services](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B3/deployment/v3/mosip/kernel) | 12.0.1-B2 |
   |[Biosdk service](https://github.com/mosip/mosip-infra/tree/v1.2.0.1-B3/deployment/v3/mosip/biosdk) | 12.0.1-B3 |
   |[Idrepo services](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B3/deployment/v3/mosip/idrepo) | 12.0.1-B2 |
   |[Pms services](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B3/deployment/v3/mosip/pms) | 12.0.1-B3 |
   |[IDA services](https://github.com/mosip/mosip-infra/blob/v1.2.0.1-B3/deployment/v3/mosip/ida) | 12.0.1-B3 |

### Install
* Install `kubectl` and `helm` utilities.
* Run `install-all.sh` to deploy signup services.
  ```
  cd helm
  ./install-all.sh
  ```
* During the execution of the `install-all.sh` script, a prompt appears requesting information regarding the presence of a public domain and a valid SSL certificate on the server.
* If the server lacks a public domain and a valid SSL certificate, it is advisable to select the `n` option. Opting it will enable the `init-container` with an `emptyDir` volume and include it in the deployment process.
* The init-container will proceed to download the server's self-signed SSL certificate and mount it to the specified location within the container's Java keystore (i.e., `cacerts`) file.
* This particular functionality caters to scenarios where the script needs to be employed on a server utilizing self-signed SSL certificates.

### Delete
* Run `delete-all.sh` to remove signup services.
  ```
  cd helm
  ./delete-all.sh
  ```

### Restart
* Run `restart-all.sh` to restart signup services.
  ```
  cd helm
  ./restart-all.sh
  ```

## APIs
API documentation is available [here](https://mosip.stoplight.io/docs/identity-provider/branches/main/6f1syzijynu40-identity-provider).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).