#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading pre-requisites started."

#i18n bundle
echo "Downloading i18n bundle files"
wget --no-check-certificate --no-cache --no-cookies $artifactory_url_env/artifactory/libs-release-local/i18n/esignet-signup-i18n-bundle.zip -O $i18n_path/esignet-signup-i18n-bundle.zip

echo "unzip i18n bundle files.."
chmod 775 $i18n_path/*

cd $i18n_path
unzip -o esignet-signup-i18n-bundle.zip
rm esignet-signup-i18n-bundle.zip

#theme bundle
echo "Downloading theme bundle files"
wget --no-check-certificate --no-cache --no-cookies $artifactory_url_env/artifactory/libs-release-local/theme/esignet-signup-theme.zip -O $theme_path/esignet-signup-theme.zip

echo "unzip i18n bundle files.."
chmod 775 $theme_path/*

cd $theme_path
unzip -o esignet-signup-theme.zip
rm esignet-signup-theme.zip

#image bundle
echo "Downloading image bundle files"
wget --no-check-certificate --no-cache --no-cookies $artifactory_url_env/artifactory/libs-release-local/image/esignet-signup-image.zip -O $image_path/esignet-signup-image.zip

echo "unzip image bundle files.."
chmod 775 $image_path/*

cd $image_path
unzip -o esignet-signup-image.zip
rm esignet-signup-image.zip


echo "Pre-requisites download completed."

workingDir=$nginx_dir/html

echo "generating env-config file"

echo "window._env_ = {" > ${workingDir}/env-config.js
awk -F '=' '{ print $1 ": \"" (ENVIRON[$1] ? ENVIRON[$1] : $2) "\"," }' ${workingDir}/env.env >> ${workingDir}/env-config.js
echo "}" >> ${workingDir}/env-config.js

echo "generation of env-config file completed!"

exec "$@"
