#!/bin/bash
# Deletes signup-with-plugins helm chart
## Usage: ./delete.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

function Deleting_signup_with_plugins() {
  NS=signup
  while true; do
      read -p "Are you sure you want to delete signup helm charts?(Y/n) " yn
      if [[ $yn = "Y" ]] || [[ $yn = "y" ]];
        then
          helm -n $NS delete signup-with-plugins
          break
        else
          break
      fi
  done
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
Deleting_signup_with_plugins   # calling function

