#!/bin/bash

#Downloads the zip/jar signup adapters
if [[ -n "$signup_plugin_url_env" ]]; then
  plugin_zip_filename=$(basename "$signup_plugin_url_env")
  wget -q "${signup_plugin_url_env}" -O "${plugins_path_env}"/"${plugin_zip_filename}"
  if file "${plugins_path_env}"/"${plugin_zip_filename}" | grep -q "Zip archive"; then
    echo "Downloaded plugins file is a zip archive. Unzipping the ${plugin_zip_filename}"
    unzip "${plugins_path_env}"/"${plugin_zip_filename}" -d "${plugins_path_env}"
  else
    echo "Downloaded plugins file ${plugin_zip_filename} is not a zip archive."
  fi
fi

# Check if the environment variables are set
if [[ -z "$plugin_name_env" ]]; then
  echo "Error: plugin_name_env is not set."
  exit 1
fi

source_file="$work_dir/plugins/$plugin_name_env"
echo "Copy plugin $source_file to $loader_path_env"
# Copy plugin file to the destination loader path
cp "$source_file" "$loader_path_env"
# Check if the copy was successful
if [[ $? -eq 0 ]]; then
  echo "Plugin file '$source_file' successfully copied to '$loader_path_env'."
else
  echo "Error: Failed to copy the plugin."
  exit 1
fi

## set active profile if not set
if [[ -z "$active_profile_env" ]]; then
  echo "Alert: active_profile_env is not set. setting to default"
  active_profile_env="default"
  export active_profile_env
fi

cd $work_dir
exec "$@"
