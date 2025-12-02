#!/bin/bash
# Installs signup-with-plugins helm chart with esignet-mock-plugin.jar
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ]; then
  export KUBECONFIG=$1
fi

set -e
set -o errexit
set -o nounset
set -o errtrace
set -o pipefail

NS=signup
CHART_VERSION=1.3.0
PLUGIN_NAME="esignet-mock-plugin.jar"

echo "Installing signup-with-plugins using $PLUGIN_NAME"

helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

echo "Creating $NS namespace (if not exists)"
kubectl create ns $NS || true

# --- Prometheus Service Monitor flag ---
while true; do
  read -p "Is Prometheus Service Monitor Operator deployed in the k8s cluster? (y/n): " response
  if [[ "$response" == "y" || "$response" == "Y" ]]; then
    servicemonitorflag=true
    break
  elif [[ "$response" == "n" || "$response" == "N" ]]; then
    servicemonitorflag=false
    break
  else
    echo "Not a correct response. Please respond with y (yes) or n (no)."
  fi
done

# --- SSL/Public domain flag ---
echo
echo "Do you have public domain & valid SSL? (Y/n)"
echo "Y: if you have public domain & valid SSL certificate"
echo "n: if you don't have a public domain and valid SSL certificate (for dev use only)"
read -p "" flag

if [ -z "$flag" ]; then
  echo "No input provided; exiting."
  exit 1
fi

ENABLE_INSECURE=''
if [ "$flag" = "n" ] || [ "$flag" = "N" ]; then
  ENABLE_INSECURE='--set enable_insecure=true'
fi

echo Installing signup-with-mock-plugin
helm -n $NS install signup mosip/signup \
  -f values.yaml --version $CHART_VERSION \
  --set plugin_name_env=$PLUGIN_NAME \
  --set metrics.serviceMonitor.enabled=$servicemonitorflag \
  --set image.repository=mosipid/signup-with-plugins --set image.tag=1.3.0 \
  $ENABLE_INSECURE --wait

# --- Wait for deployments to be ready ---
kubectl -n $NS get deploy -o name | xargs -n1 -t kubectl -n $NS rollout status

echo " Installed signup-with-plugin with $PLUGIN_NAME successfully"