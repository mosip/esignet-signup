#!/bin/bash
# Installs mock-mv
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=mock-smtp
CHART_VERSION=1.0.0

echo Create $NS namespace
kubectl create ns $NS

function mock_smtp() {
  echo Istio label
  kubectl label ns $NS istio-injection=enabled --overwrite
  helm repo update

  COPY_UTIL=../copy_cm_func.sh
  $COPY_UTIL configmap esignet-global esignet $NS

  SMTP_HOST=$(kubectl get cm esignet-global -n esignet -o jsonpath={.data.mosip-smtp-host})

  echo Installing mock-smtp
  helm -n $NS install mock-smtp mosip/mock-smtp --set istio.hosts\[0\]=$SMTP_HOST --version $CHART_VERSION

  kubectl -n $NS get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed mock-smtp services
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
mock_smtp   # calling function
