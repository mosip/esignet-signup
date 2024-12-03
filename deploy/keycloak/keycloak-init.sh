#!/bin/bash
# Initialises signup keycloak-init
## Usage: ./keycloak-init.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes


NS=signup
CHART_VERSION=0.0.1-develop
COPY_UTIL=../copy_cm_func.sh

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

kubectl create ns $NS || true

echo "checking if mosip-pms-client, mosip-ida-client & mpartner_default_auth client is created already"
IAMHOST_URL=$(kubectl -n esignet get cm esignet-global -o jsonpath={.data.mosip-iam-external-host})
SIGNUP_CLIENT_SECRET_KEY='mosip_signup_client_secret'
SIGNUP_CLIENT_SECRET_VALUE=$(kubectl -n keycloak get secrets keycloak-client-secrets -o jsonpath={.data.$SIGNUP_CLIENT_SECRET_KEY} | base64 -d)
echo "Copying keycloak configmaps and secret"
$COPY_UTIL configmap keycloak-host keycloak $NS
$COPY_UTIL configmap keycloak-env-vars keycloak $NS
$COPY_UTIL secret keycloak keycloak $NS

echo "creating and adding roles to keycloak pms & mpartner_default_auth clients for ESIGNET"
kubectl -n $NS delete secret --ignore-not-found=true keycloak-client-secrets
helm -n $NS delete signup-keycloak-init || true
helm -n $NS install signup-keycloak-init mosip/keycloak-init \
  -f keycloak-init-values.yaml \
  --set clientSecrets[0].name="$SIGNUP_CLIENT_SECRET_KEY" \
  --set clientSecrets[0].secret="$SIGNUP_CLIENT_SECRET_VALUE" \
  --version $CHART_VERSION --wait --wait-for-jobs

SIGNUP_CLIENT_SECRET_VALUE=$(kubectl -n $NS get secrets keycloak-client-secrets -o jsonpath={.data.$SIGNUP_CLIENT_SECRET_KEY})
# Check if the secret exists
if kubectl get secret keycloak-client-secrets -n keycloak >/dev/null 2>&1; then
  echo "Secret 'keycloak-client-secrets' exists. Performing secret update..."
  kubectl -n keycloak get secret keycloak-client-secrets -o json |
  jq ".data[\"$SIGNUP_CLIENT_SECRET_KEY\"]=\"$SIGNUP_CLIENT_SECRET_VALUE\"" |
  kubectl apply -f -
else
  echo "Secret 'keycloak-client-secrets' does not exist. Copying the secret to the keycloak namespace."
  $COPY_UTIL secret keycloak-client-secrets $NS keycloak
fi
