# esignet-signup

signup-service is part of the esignet modules, but has a separate Helm chart so as to install and manage it in a completely independent namespace.

Below diagram depicts the high level deployment architecture for signup service with MOSIP ID-repo.

![](docs/signup-with-mosip-id-repo.png)

### Configurations
Signup service and signup UI currently supports default [ID schema](docs/id-schema.json) only.

**Note:**
Work is in progress to support any ID schema and also to connect with any registry services.

With respect to the default ID schema, below MOSIP configurations are required to be updated.

#### admin-default.properties
``
mosip.admin.masterdata.lang-code=eng,khm
``

#### application-default.properties
```
mosip.mandatory-languages=eng,khm

mosip.optional-languages=

mosip.default.template-languages=eng,khm
```

#### id-authentication-default.properties
```
request.idtypes.allowed=UIN,HANDLE

request.idtypes.allowed.internalauth=UIN

ida.mosip.external.auth.filter.classes.in.execution.order=io.mosip.authentication.hotlistfilter.impl.PartnerIdHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.IndividualIdHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.DeviceProviderHotlistFilterImpl,io.mosip.authentication.hotlistfilter.impl.DeviceHotlistFilterImpl,io.mosip.authentication.authtypelockfilter.impl.AuthTypeLockFilterImpl

mosip.ida.handle-types.regex={ '@phone' : '^\\+91[1-9][0-9]{7,9}@phone$' }
```

####  id-repository-default.properties
```
mosip.idrepo.credential.request.enable-convention-based-id=true

mosip.idrepo.identity.disable-uin-based-credential-request=true

mosip.idrepo.vid.disable-support=true

mosip.identity.fieldid.handle-postfix.mapping={'phone':'@phone'}
```

#### kernel-default.properties
``
mosip.kernel.sms.country.code=+91
``



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
* Run `install.sh` to run the partner-onboarder to create the signup-oidc
 ```
  cd partner-onboarder
  ./install.sh
  ```
* Run `install-all.sh` to deploy signup services.
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
## Partner onboarding
* Perform Partner onboarding for esignet Signup OIDC client using [steps](partner-onboarder/README.md) only if mosip-identity plugin is used.  

## APIs
API documentation is available [here](https://mosip.stoplight.io/docs/identity-provider/branches/signupV1/t9tvfbteqqokf-e-signet-signup-portal-ap-is).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
