# Script to initialize the DB.
## Usage: ./init_db.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

# Function to initialize the DB
function initialize_db() {
  NS=signup
  CHART_VERSION=1.1.0-develop
  helm repo update

  # Confirm if the user wants to initialize DB scripts
  while true; do
    read -p "Are the modules of the MOSIP platform already deployed? (Y/n): " yn
    if [[ "$yn" == "Y" || "$yn" == "y" ]]; then
      echo "Exiting as MOSIP platform modules are already deployed.No need to initialize db again"
      exit 0
    elif [[ "$yn" == "N" || "$yn" == "n" ]]; then
      echo "Initializing DB scripts for MOSIP_KERNEL and MOSIP_AUDIT, because mosip platform modules are not deployed yet"
      break
    else
      echo "Invalid input. Please enter Y for Yes or N for No."
    fi
  done

  while true; do
      read -p "Please confirm with "Y" once init-values.yaml is updated correctly with tag, postgres host details else "N" to exit installation: " ans
      if [ "$ans" = "Y" ] || [ "$ans" = "y" ]; then
          break
      elif [ "$ans" = "N" ] || [ "$ans" = "n" ]; then
          exit 1
      else
          echo "Please provide a correct option (Y or N)"
      fi
  done

  # Prompt for dbuserPassword
  echo "Please provide the dbuserPassword"
  read -s dbuserPassword
  if [ -z "$dbuserPassword" ]; then
    echo "ERROR: dbuserPassword not specified; EXITING."
    exit 1
  fi

  # Initialize DB
  echo "Removing any existing installation..."
  helm -n $NS delete postgres-init || true
  kubectl -n $NS delete secret db-common-secrets || true
  ./copy_cm_func.sh secret postgres-postgresql postgres $NS

  echo "Initializing DB..."
  helm -n $NS install postgres-init mosip/postgres-init -f init_values.yaml \
    --version $CHART_VERSION \
    --set dbUserPasswords.dbuserPassword="$dbuserPassword" \
    --wait --wait-for-jobs

  echo "Database initialization complete."
  return 0
}

# Set commands for error handling
set -e
set -o errexit   ## exit the script if any statement returns a non-true return value
set -o nounset   ## exit the script if you try to use an uninitialized variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes

# Call the function
initialize_db
