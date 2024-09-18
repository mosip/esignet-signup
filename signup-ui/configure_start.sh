#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading pre-requisites started."

# Check if $i18n_url_env is not empty
if [[ -n "$i18n_url_env" ]]; then
    echo "i18n_url_env is set: $i18n_url_env"
    wget --no-check-certificate --no-cache --no-cookies $i18n_url_env -O $i18n_path/esignet-signup-i18n-bundle.zip

    echo "unzip i18n bundle files.."
    chmod 775 $i18n_path/*
    cd $i18n_path
    unzip -o esignet-signup-i18n-bundle.zip
    rm esignet-signup-i18n-bundle.zip
    echo "unzip i18n bundle completed."
fi

# Check if $theme_url_env is not empty
if [[ -n "$theme_url_env" ]]; then
    echo "theme_url_env is set: $theme_url_env"
    wget --no-check-certificate --no-cache --no-cookies $theme_url_env -O $theme_path/esignet-signup-theme.zip

    echo "unzip theme files.."
    chmod 775 $theme_path/*
    cd $theme_path
    unzip -o esignet-signup-theme.zip
    rm esignet-signup-theme.zip
    echo "unzip theme completed."
fi

# Check if $images_url_env is not empty
if [[ -n "$images_url_env" ]]; then
    echo "images_url_env is set: $images_url_env"
    wget --no-check-certificate --no-cache --no-cookies $images_url_env -O $image_path/esignet-signup-image.zip

    echo "unzip image files.."
    chmod 775 $image_path/*
    cd $image_path
    unzip -o esignet-signup-image.zip
    rm esignet-signup-image.zip
    echo "unzip image completed."
fi

echo "Pre-requisites download completed."

workingDir=$nginx_dir/html

echo "generating env-config file"

echo "window._env_ = {" > ${workingDir}/env-config.js
awk -F '=' '{ print $1 ": \"" (ENVIRON[$1] ? ENVIRON[$1] : $2) "\"," }' ${workingDir}/env.env >> ${workingDir}/env-config.js
echo "}" >> ${workingDir}/env-config.js

echo "generation of env-config file completed!"

exec "$@"
