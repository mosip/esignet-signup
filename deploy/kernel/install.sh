#!/bin/bash
# Installs all kernel helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=kernel
CHART_VERSION=12.0.1

echo Create $NS namespace
kubectl create ns $NS

function installing_kernel() {
  echo Istio label
  kubectl label ns $NS istio-injection=enabled --overwrite
  helm repo update

  COPY_UTIL=../copy_cm_func.sh
  $COPY_UTIL configmap artifactory-share artifactory $NS
  $COPY_UTIL configmap config-server-share config-server $NS
  $COPY_UTIL configmap esignet-global esignet $NS

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

  echo Installing authmanager
  helm -n $NS install authmanager mosip/authmanager --version $CHART_VERSION $ENABLE_INSECURE

  echo Installing auditmanager
  helm -n $NS install auditmanager mosip/auditmanager --version $CHART_VERSION $ENABLE_INSECURE

  echo Installing otpmanager
  helm -n $NS install otpmanager mosip/otpmanager --version $CHART_VERSION

  echo Installing notifier
  helm -n $NS install notifier mosip/notifier --version $CHART_VERSION

  # Array of deployment names
  DEPLOYMENTS=("authmanager" "auditmanager" "otpmanager" "notifier")

  # Patch all deployments to use esignet-global as configMapRef
  for DEPLOYMENT in "${DEPLOYMENTS[@]}"; do
    echo Patching $DEPLOYMENT to use esignet-global ConfigMap
    kubectl -n $NS patch deployment $DEPLOYMENT \
      --type=json \
      -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/envFrom/0/configMapRef/name", "value": "esignet-global"}]'
  done

  kubectl -n $NS set env deployment/notifier \
    MOSIP_KERNEL_SMS_NUMBER_MIN_LENGTH=7 \
    MOSIP_KERNEL_SMS_NUMBER_MAX_LENGTH=10

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed kernel services
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_kernel   # calling function
