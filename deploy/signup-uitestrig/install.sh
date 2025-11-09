#!/usr/bin/env bash
# Installs uitestrig automation (UITestRig)
# Usage: ./install.sh [kubeconfig]

set -euo pipefail
set -o errtrace

if [ $# -ge 1 ] ; then
  export KUBECONFIG="$1"
fi

trap 'echo "Script aborted due to an error. Please check the output above."; exit 1' ERR
trap 'echo "Exiting."; exit' INT TERM

read -r -p "Please confirm that 'values.yaml' has been updated with the required configuration (enter Y to continue): " confirm
if [[ "${confirm:-}" != "Y" && "${confirm:-}" != "y" ]]; then
  echo "Please update values.yaml before running this script. Exiting..."
  exit 1
fi

NS="signup-uitestrig"
CHART_VERSION="0.0.1-develop"
COPY_UTIL="../copy_cm_func.sh"

echo "Ensure namespace $NS exists (creating if missing)..."
if ! kubectl get ns "$NS" >/dev/null 2>&1; then
  kubectl create ns "$NS"
else
  echo "Namespace $NS already exists."
fi

function installing_uitestrig() {
  echo "Labeling namespace for Istio (disabled)"
  kubectl label ns "$NS" istio-injection=disabled --overwrite

  echo "Updating Helm repos..."
  helm repo update

  echo "Copy configmaps"
  if [ -x "$COPY_UTIL" ]; then
    "$COPY_UTIL" configmap esignet-global esignet "$NS"
    "$COPY_UTIL" configmap keycloak-host signup "$NS"
    "$COPY_UTIL" configmap db signup "$NS"
  else
    echo "Warning: copy utility '$COPY_UTIL' not found or not executable. Skipping configmap copy."
  fi

  echo "Copy secrets"
  if [ -x "$COPY_UTIL" ]; then
    "$COPY_UTIL" secret keycloak-client-secrets keycloak "$NS"
    "$COPY_UTIL" secret s3 s3 "$NS"
    "$COPY_UTIL" secret postgres-postgresql postgres "$NS"
  else
    echo "Warning: copy utility '$COPY_UTIL' not found or not executable. Skipping secret copy."
  fi

  echo "Delete s3, db, & uitestrig configmaps if they exist"
  kubectl -n "$NS" delete configmap s3 db uitestrig --ignore-not-found=true

  DB_HOST=$(kubectl -n esignet get cm esignet-global -o json | jq -r '.data["mosip-db-host"] // .data["db-host"] // empty' || true)
  if [[ -z "$DB_HOST" ]]; then
    DB_HOST=$(kubectl -n esignet get cm esignet-global -o json | jq -r '.data["mosip-api-internal-host"] // empty' || true)
  fi

  API_INTERNAL_HOST=$(kubectl -n esignet get cm esignet-global -o json | jq -r '.data["mosip-api-internal-host"] // empty' || true)
  ENV_USER="unknown"
  if [[ -n "$API_INTERNAL_HOST" ]]; then
    ENV_USER=$(awk -F'.' '/api-internal/ {print $1"."$2; exit}' <<<"$API_INTERNAL_HOST" || true)
    if [[ -z "$ENV_USER" ]]; then
      ENV_USER=$(cut -d'.' -f1-2 <<<"$API_INTERNAL_HOST" || true)
    fi
  fi

  if [[ -z "$DB_HOST" ]]; then
    echo "ERROR: Could not determine DB_HOST from esignet/esignet-global configmap."
    exit 1
  fi

  read -r -p "Please enter the time (hour 0-23) to run the cronjob every day: " time
  if [[ -z "${time:-}" || "$time" -lt 0 || "$time" -gt 23 ]]; then
    echo "Invalid time. Must be between 0-23."
    exit 1
  fi

  read -r -p "Do you have public domain & valid SSL? (Y/n) [default: Y]: " flag
  flag="${flag:-Y}"
  ENABLE_INSECURE=""
  if [[ "$flag" = "n" || "$flag" = "N" ]]; then
    ENABLE_INSECURE="--set enable_insecure=true"
  fi

  read -r -p "Please provide the retention days to remove old reports (Default: 3): " reportExpirationInDays
  reportExpirationInDays="${reportExpirationInDays:-3}"

  read -r -p "Please provide slack webhook URL to notify server end issues on your slack channel: " slackWebhookUrl
  if [[ -z "${slackWebhookUrl:-}" ]]; then
    echo "Slack webhook URL not provided; EXITING."
    exit 1
  fi

  read -r -p "Is the esignet service deployed? (yes/no): " eSignetDeployed
  eSignetDeployed="${eSignetDeployed,,}"
  if [[ "$eSignetDeployed" == "yes" ]]; then
    echo "esignet service is deployed. Proceeding with installation..."
  else
    echo "esignet service is not deployed. Skipping esignet related test-cases..."
  fi

  read -r -p "Is values.yaml for uitestrig chart set correctly? (Y/n) [default: Y]: " yn
  yn="${yn:-Y}"
  if [[ "$yn" != "Y" && "$yn" != "y" ]]; then
    echo "values.yaml not confirmed. Exiting."
    exit 1
  fi

  read -r -p "Do you have S3 details for storing uitestrig reports? (Y/n): " ans
  ans="${ans:-Y}"

  if [[ "$ans" == "y" || "$ans" == "Y" ]]; then
    read -r -p "Please provide S3 host: " s3_host
    read -r -p "Please provide S3 region (press Enter to skip): " s3_region
    read -r -p "Please provide S3 access key: " s3_user_key
    S3_OPTION="--set uitestrig.configmaps.s3.s3-host=$s3_host --set uitestrig.configmaps.s3.s3-user-key=$s3_user_key"
    [[ -n "$s3_region" ]] && S3_OPTION="$S3_OPTION --set uitestrig.configmaps.s3.s3-region=$s3_region"
    push_reports_to_s3="yes"
  else
    push_reports_to_s3="no"
    NFS_OPTION=""
  fi

  echo "Installing signup uitestrig Helm chart..."
  helm -n "$NS" install signup-uitestrig mosip/uitestrig \
    --set "crontime=0 $time * * *" \
    -f values.yaml \
    --version "$CHART_VERSION" \
    ${NFS_OPTION:-} \
    ${S3_OPTION:-} \
    --set uitestrig.variables.push_reports_to_s3="$push_reports_to_s3" \
    --set-string uitestrig.configmaps.db.db-server="$DB_HOST" \
    --set-string uitestrig.configmaps.db.db-su-user="postgres" \
    --set-string uitestrig.configmaps.db.db-port="5432" \
    --set-string uitestrig.configmaps.uitestrig.ENV_USER="$ENV_USER" \
    --set-string uitestrig.configmaps.uitestrig.ENV_ENDPOINT="https://$API_INTERNAL_HOST" \
    --set-string uitestrig.configmaps.uitestrig.reportExpirationInDays="$reportExpirationInDays" \
    --set-string uitestrig.configmaps.uitestrig.slack-webhook-url="$slackWebhookUrl" \
    --set-string uitestrig.configmaps.uitestrig.eSignetDeployed="$eSignetDeployed" \
    --set-string uitestrig.configmaps.uitestrig.NS="$NS" \
    $ENABLE_INSECURE

  echo "Installed signup uitestrig."
}

installing_uitestrig
