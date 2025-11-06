#!/bin/bash
## Installs signup services in correct order
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

ROOT_DIR=$(pwd)

function installing_signup() {

  echo "Select the plugin type for signup:"
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

  echo "Installing signup services..."

  # Install selected plugin first
  cd "$PLUGIN_PATH"
  ./install.sh

  # Install signup-ui next
  cd "$ROOT_DIR/signup-ui"
  ./install.sh

  echo "All signup services deployed successfully."
  return 0
}

# Error handling
set -e
set -o errexit
set -o nounset
set -o errtrace
set -o pipefail

installing_signup