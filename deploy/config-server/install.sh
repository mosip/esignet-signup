#!/bin/bash
# Installs config-server
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes

NS=config-server
CHART_VERSION=12.0.1

    echo Create $NS namespace
    kubectl create ns $NS || true

    echo Istio label
    kubectl label ns $NS istio-injection=enabled --overwrite
    helm repo update

    COPY_UTIL=../copy_cm_func.sh
    $COPY_UTIL configmap keycloak-host keycloak $NS
    $COPY_UTIL configmap esignet-global esignet $NS
    $COPY_UTIL configmap msg-gateway msg-gateways $NS

    $COPY_UTIL secret keycloak keycloak $NS
    $COPY_UTIL secret db-common-secrets esignet $NS
    $COPY_UTIL secret keycloak-client-secrets keycloak $NS
    $COPY_UTIL secret msg-gateway msg-gateways $NS

    echo Installing config-server
    helm -n $NS install config-server mosip/config-server -f values.yaml --wait --version $CHART_VERSION
    echo Installed Config-server.
