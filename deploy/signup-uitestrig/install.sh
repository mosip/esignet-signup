#!/bin/bash
# Installs uitestrig automation
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=signup
CHART_VERSION=0.0.1-develop
COPY_UTIL=../copy_cm_func.sh

echo Create $NS namespace
kubectl create ns $NS

function installing_uitestrig() {
  ENV_NAME=$( kubectl -n default get cm esignet-global -o json |jq -r '.data."installation-domain"')

  read -p "Please enter the time(hr) to run the cronjob every day (time: 0-23) : " time
  if [ -z "$time" ]; then
     echo "ERROR: Time cannot be empty; EXITING;";
     exit 1;
  fi
  if ! [ $time -eq $time ] 2>/dev/null; then
     echo "ERROR: Time $time is not a number; EXITING;";
     exit 1;
  fi
  if [ $time -gt 23 ] || [ $time -lt 0 ] ; then
     echo "ERROR: Time should be in range ( 0-23 ); EXITING;";
     exit 1;
  fi

  echo "Do you have public domain & valid SSL? (Y/n) "
  echo "Y: if you have public domain & valid ssl certificate"
  echo "n: if you don't have public domain & valid ssl certificate"
  read -p "" flag

  if [ -z "$flag" ]; then
    echo "'flag' was provided; EXITING;"
    exit 1;
  fi
  ENABLE_INSECURE=''
  if [ "$flag" = "n" ]; then
    ENABLE_INSECURE='--set uitestrig.configmaps.uitestrig.ENABLE_INSECURE=true';
  fi

  echo Istio label
  kubectl label ns $NS istio-injection=disabled --overwrite
  helm repo update

  echo Copy configmaps
  $COPY_UTIL configmap esignet-global default $NS
  $COPY_UTIL configmap keycloak-host keycloak $NS
  $COPY_UTIL configmap artifactory-share artifactory $NS
  $COPY_UTIL configmap config-server-share config-server $NS

  echo Copy secrets
  $COPY_UTIL secret keycloak-client-secrets keycloak $NS
  $COPY_UTIL secret s3 s3 $NS
  $COPY_UTIL secret postgres-postgresql postgres $NS

  DB_HOST=$( kubectl -n default get cm esignet-global -o json  |jq -r '.data."mosip-api-internal-host"' )
  API_INTERNAL_HOST=$( kubectl -n default get cm esignet-global -o json  |jq -r '.data."mosip-api-internal-host"' )

  echo Installing signup uitestrig
  helm -n $NS install signup-uitestrig mosip/uitestrig \
  --set crontime="0 $time * * *" \
  -f values.yaml  \
  --version $CHART_VERSION \
  --set uitestrig.configmaps.s3.s3-host='http://minio.minio:9000' \
  --set uitestrig.configmaps.s3.s3-user-key='signup' \
  --set uitestrig.configmaps.s3.s3-region='' \
  --set uitestrig.configmaps.db.db-server="$DB_HOST" \
  --set uitestrig.configmaps.db.db-su-user="postgres" \
  --set uitestrig.configmaps.db.db-port="5432" \
  --set uitestrig.configmaps.uitestrig.apiInternalEndPoint="https://$API_INTERNAL_HOST" \
  --set uitestrig.configmaps.uitestrig.apiEnvUser="$API_INTERNAL_HOST" \
  --set uitestrig.configmaps.uitestrig.NS="$NS" \
  $ENABLE_INSECURE

  echo Installed signup uitestrig
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_uitestrig   # calling function