#!/bin/bash
# Installs oidc-ui helm charts
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=signup
CHART_VERSION=1.0.2

echo Create $NS namespace
kubectl create ns $NS

function installing_signup-ui() {
  echo Istio label
  kubectl label ns $NS istio-injection=enabled --overwrite

  helm repo add mosip https://mosip.github.io/mosip-helm
  helm repo update

  echo Copy configmaps
  ./copy_cm.sh

  SIGNUP_HOST=$(kubectl get cm global -o jsonpath={.data.mosip-signup-host})

  echo Installing SIGNUP UI
  helm -n $NS install signup-ui mosip/signup-ui \
  --set signup_ui.configmaps.signup-ui.REACT_APP_API_BASE_URL="http://signup.$NS/v1/signup" \
  --set signup_ui.configmaps.signup-ui.REACT_APP_SBI_DOMAIN_URI="http://signup.$NS" \
  --set signup_ui.configmaps.signup-ui.SIGNUP_UI_PUBLIC_URL=''\
  --set istio.hosts\[0\]=$SIGNUP_HOST \
  --version $CHART_VERSION 

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status

  echo Installed signup-ui
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_signup-ui   # calling function
