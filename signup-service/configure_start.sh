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

IFS=',' read -ra plugin_array <<< "$plugin_name_env"
for plugin_name in "${plugin_array[@]}"; do
  plugin_name_trimmed=$(echo "$plugin_name" | xargs)  # Trim spaces
  source_file="$work_dir/plugins/$plugin_name_trimmed"
  echo "Copying plugin: $source_file to $loader_path_env"
  if [[ -f "$source_file" ]]; then
    cp "$source_file" "$loader_path_env"
    echo "Plugin '$plugin_name_trimmed' copied successfully."
  else
    echo "Error: Plugin '$plugin_name_trimmed' not found at '$source_file'."
    exit 1
  fi
done

## set active profile if not set
if [[ -z "$active_profile_env" ]]; then
  echo "Alert: active_profile_env is not set. setting to default"
  active_profile_env="default"
  export active_profile_env
fi

cd $work_dir
exec "$@"
