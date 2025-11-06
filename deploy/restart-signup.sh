#!/bin/bash
# Restarts signup services in correct order
## Usage: ./restart-all.sh [kubeconfig]

if [ $# -ge 1 ]; then
  export KUBECONFIG=$1
fi

ROOT_DIR=$(pwd)

function Restarting_All() {
  echo "Select the plugin type for signup to restart:"
  echo "1) MOSIP ID Plugin"
  echo "2) Mock Plugin"
  read -p "Enter your choice (1 or 2): " choice

  case $choice in
    1)
      PLUGIN_PATH="$ROOT_DIR/signup-with-plugins/signup-with-mosipid-plugin"
      ;;
    2)
      PLUGIN_PATH="$ROOT_DIR/signup-with-plugins/signup-with-mock-plugin"
      ;;
    *)
      echo "Invalid choice! Exiting..."
      exit 1
      ;;
  esac

  echo "Restarting signup services..."

  # Restart plugin first
  cd "$PLUGIN_PATH"
  if [ -f "./restart.sh" ]; then
    ./restart.sh
  else
    echo "restart.sh not found in $PLUGIN_PATH"
  fi

  # Restart signup-ui next
  cd "$ROOT_DIR/signup-ui"
  if [ -f "./restart.sh" ]; then
    ./restart.sh
  else
    echo "restart.sh not found in signup-ui"
  fi

  echo "All signup services restarted successfully."
  return 0
}

# Error handling
set -e
set -o errexit   ## exit if any statement fails
set -o nounset   ## exit if variable is undefined
set -o errtrace  ## trace ERR through functions
set -o pipefail  ## trace ERR through pipes

Restarting_All   # calling function
