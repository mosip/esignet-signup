#!/bin/bash
# Installs all signup helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

SOFTHSM_NS=softhsm
SOFTHSM_CHART_VERSION=12.0.1

echo Create $SOFTHSM_NS namespace
kubectl create ns $SOFTHSM_NS

NS=signup
CHART_VERSION=1.0.2

SIGNUP_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-signup-host})

echo Create $NS namespace
kubectl create ns $NS

function installing_signup() {
  echo Istio label
  kubectl label ns $SOFTHSM_NS istio-injection=enabled --overwrite
  helm repo add mosip https://mosip.github.io/mosip-helm
  helm repo update

  echo Installing Softhsm for signup
  helm -n $SOFTHSM_NS install softhsm-signup mosip/softhsm -f softhsm-values.yaml --version $SOFTHSM_CHART_VERSION --wait
  echo Installed Softhsm for signup

  echo Copy configmaps
  ./copy_cm_func.sh configmap global default config-server

  echo Copy secrets
  ./copy_cm_func.sh secret softhsm-signup softhsm config-server

  kubectl -n config-server set env --keys=mosip-signup-host --from configmap/global deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server set env --keys=security-pin --from secret/softhsm-signup deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_SOFTHSM_SIGNUP_
  kubectl -n config-server rollout restart deploy config-server
  kubectl -n config-server get deploy -o name |  xargs -n1 -t  kubectl -n config-server rollout status

  ./keycloak-init.sh

  echo Please enter the recaptcha admin site key for domain $SIGNUP_HOST
  read SIGNUP_SITE_KEY
  echo Please enter the recaptcha admin secret key for domain $SIGNUP_HOST
  read SIGNUP_SECRET_KEY

  echo Setting up captcha secrets
  kubectl -n $NS create secret generic signup-captcha --from-literal=signup-captcha-site-key=$SIGNUP_SITE_KEY --from-literal=signup-captcha-secret-key=$SIGNUP_SECRET_KEY --dry-run=client -o yaml | kubectl apply -f -

  echo Setting up dummy values for signup misp license key
  kubectl create secret generic signup-misp-onboarder-key -n $NS --from-literal=mosip-signup-misp-key='' --dry-run=client -o yaml | kubectl apply -f -

  ./copy_cm_func.sh secret signup-misp-onboarder-key signup config-server

  echo Copy configmaps
  ./copy_cm.sh

  echo copy secrets
  ./copy_secrets.sh

  SIGNUP_CLIENT_SECRET_KEY='mosip_signup_client_secret'
  SIGNUP_CLIENT_SECRET_VALUE=$(kubectl get secret keycloak-client-secrets -n signup -o jsonpath='{.data.mosip_signup_client_secret}')
  echo $SIGNUP_CLIENT_SECRET_VALUE
  kubectl patch secret keycloak-client-secrets --namespace=config-server --type=json -p='[{"op": "add", "path": "/data/'$SIGNUP_CLIENT_SECRET_KEY'", "value": "'$SIGNUP_CLIENT_SECRET_VALUE'"}]'

  kubectl -n config-server set env --keys=mosip_signup_client_secret --from secret/keycloak-client-secrets deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server set env --keys=mosip-signup-host --from configmap/global deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server set env --keys=signup-captcha-site-key --from secret/signup-captcha deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server set env --keys=signup-captcha-secret-key --from secret/signup-captcha deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_
  kubectl -n config-server set env --keys=mosip-signup-misp-key --from secret/signup-misp-onboarder-key deployment/config-server --prefix=SPRING_CLOUD_CONFIG_SERVER_OVERRIDES_

  kubectl -n config-server get deploy -o name |  xargs -n1 -t  kubectl -n config-server rollout status

  echo "Do you have public domain & valid SSL? (Y/n) "
  echo "Y: if you have public domain & valid ssl certificate"
  echo "n: If you don't have a public domain and a valid SSL certificate. Note: It is recommended to use this option only in development environments."
  read -p "" flag

  if [ -z "$flag" ]; then
    echo "'flag' was provided; EXITING;"
    exit 1;
  fi
  ENABLE_INSECURE=''
  if [ "$flag" = "n" ]; then
    ENABLE_INSECURE='--set enable_insecure=true';
  fi

  echo Installing signup
  helm -n $NS install signup mosip/signup --version $CHART_VERSION $ENABLE_INSECURE

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed signup
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_signup   # calling function
