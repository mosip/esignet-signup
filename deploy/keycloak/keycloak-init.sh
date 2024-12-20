#!/bin/bash
# Initialises signup keycloak-init and manages secrets in keycloak-client-secrets
## Usage: ./keycloak-init.sh [kubeconfig]

if [ $# -ge 1 ]; then
  export KUBECONFIG=$1
fi

# Set commands for error handling
set -e
set -o errexit   ## Exit the script if any statement returns a non-true return value
set -o nounset   ## Exit the script if you try to use an uninitialized variable
set -o errtrace  # Trace ERR through 'time command' and other functions
set -o pipefail  # Trace ERR through pipes

NS=signup
CHART_VERSION=0.0.1-develop
COPY_UTIL=../copy_cm_func.sh

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

kubectl create ns $NS || true

echo "mosip-signup-client secret  is created already"
SIGNUP_CLIENT_SECRET_KEY='mosip_signup_client_secret'
SIGNUP_CLIENT_SECRET_VALUE=$(kubectl -n keycloak get secrets keycloak-client-secrets -o jsonpath={.data.$SIGNUP_CLIENT_SECRET_KEY} | base64 -d)

echo "Copying keycloak configmaps and secret"
$COPY_UTIL configmap keycloak-host keycloak $NS
$COPY_UTIL configmap keycloak-env-vars keycloak $NS
$COPY_UTIL secret keycloak keycloak $NS

echo "Creating and adding roles to mosip-signup-client  for SIGNUP"
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

# Process remaining secrets for Kernel
SECRETS=(
  "mosip_prereg_client_secret"
  "mosip_auth_client_secret"
  "mosip_ida_client_secret"
  "mosip_admin_client_secret"
)

for SECRET in "${SECRETS[@]}"; do
  read -p "Enter value for $SECRET (leave empty to create an empty key): " SECRET_VALUE
  if [[ -z "$SECRET_VALUE" ]]; then
    echo "No value entered for $SECRET. Creating it with an empty value."
    SECRET_VALUE=""
    kubectl patch secret keycloak-client-secrets --namespace=$NS --type=json -p='[{"op": "add", "path": "/data/'$SECRET'", "value": "'$SECRET_VALUE'"}]'
    $COPY_UTIL secret keycloak-client-secrets $NS keycloak
  fi
done

echo "All specified secrets have been updated in keycloak-client-secrets."
