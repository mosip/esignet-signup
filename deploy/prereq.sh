#!/bin/bash

# Installs signup services in correct order
## Usage: ./install-all.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

ROOT_DIR=`pwd`
NS=signup

echo "This script considers below mentioned points:"
echo "1. Keycloak is installed in keycloak namespace and already initialised once during esignet pre-requisites initialisation."
echo "2. Redis is installed and relevant secret and configmap is present in redis namespace as part of esignet pre-requisites installation."
echo "3. Kafka is installed in kafka namespace as part of esignet pre-requisites installation."

function installing_prereq() {
  helm repo add mosip https://mosip.github.io/mosip-helm
  helm repo update

  echo Create $NS namespace
  kubectl create ns $NS || true

  ./copy_cm_func.sh configmap esignet-global esignet $NS
  echo "Sucessfully copied esignet-global configmap from esignet namespace to "$NS" "


  ./copy_cm_func.sh configmap redis-config redis $NS
  ./copy_cm_func.sh secret redis redis $NS
  echo "Sucessfully copied configmaps and secrets required to connect to the redis server from redis namespace which is also shared with esignet"

  echo "Note: By default pointing to the Kafka installed in kafka namespace used by esignet service as well. In case want to change the same, deploy new kafka server and update in signup application properties."

  cd $ROOT_DIR/keycloak
  ./keycloak-init.sh

  SIGNUP_HOST=$(kubectl -n esignet get cm esignet-global -o jsonpath={.data.mosip-signup-host})
  echo "Please enter the recaptcha admin site key for domain "$SIGNUP_HOST""
  read SSITE_KEY
  echo Please enter the recaptcha admin secret key for domain $SIGNUP_HOST
  read SSECRET_KEY

  echo Setting up captcha secrets
  kubectl -n $NS create secret generic signup-captcha --from-literal=signup-captcha-site-key=$SSITE_KEY --from-literal=signup-captcha-secret-key=$SSECRET_KEY --dry-run=client -o yaml | kubectl apply -f -

  echo creating empty signup-keystore-password secret
  kubectl -n $NS create secret generic signup-keystore-password --from-literal=signup-keystore-password='' --dry-run=client -o yaml | kubectl apply -f -

  echo creating empty signup-keystore secret
  kubectl -n $NS create secret generic signupoidc --from-literal=oidckeystore.p12='' --dry-run=client -o yaml | kubectl apply -f -

  echo All signup services pre-requisites deployed sucessfully.
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_prereq   # calling function
