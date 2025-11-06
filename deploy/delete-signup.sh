#!/bin/bash
## Deletes signup services in correct order
## Usage: ./delete.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

ROOT_DIR=$(pwd)

function deleting_signup() {

  echo "Select the plugin type for signup to delete:"
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

  echo "Deleting signup services..."

  # Delete signup-ui first (reverse of install order)
  cd "$ROOT_DIR/signup-ui"
  if [ -f "./delete.sh" ]; then
    ./delete.sh
  else
    echo "Warning: delete.sh not found in signup-ui"
  fi

  # Delete selected plugin
  cd "$PLUGIN_PATH"
  if [ -f "./delete.sh" ]; then
    ./delete.sh
  else
    echo "Warning: delete.sh not found in selected plugin path"
  fi

  echo "All signup services deleted successfully."
  return 0
}

# Error handling
set -e
set -o errexit
set -o nounset
set -o errtrace
set -o pipefail

deleting_signup
