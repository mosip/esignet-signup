# Partner Onboarder

## Overview
Creates and onboards eSignet signup OIDC client. Refer [mosip-onboarding repo](https://github.com/mosip/mosip-onboarding).

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
* When install.sh runs,it uses https://github.com/mosip/mosip-onboarding/blob/release-1.3.x/certs/create-signing-certs.sh to generate appropriate keypair and keystore.p12 file for the partner.

* After generating the same it uses the public key in JWK format in the OIDC client creation process.
* The keystore file(.p12) ,which contains the private-key is then stored as a secret in the k8s cluster,from where it is mounted on to the esignet-signup pod in the esignet namespace.
# Troubleshooting:
* Once onboarder job is completed, detailed `html report` is prepared and stored at provided S3 bucket / NFS directory. 
* Once onboarder helm installation is complted, please check the reports to confirm successful onboarding.

### Commonly found issues 
1. KER-ATH-401: Authentication Failed
    Resolution: You need to provide correct secretkey for mosip-deployment-client.
1. Certificate dates are not valid
    Resolution: Check with admin regarding certificate renewal through re-onboarding.
