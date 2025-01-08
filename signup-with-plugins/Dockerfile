FROM mosipdev/signup-service

# copy all files under target to the plugins folder
COPY ./target/*-plugin.jar ${plugins_path_env}
COPY ./target/kernel-auth-adapter-lite.jar ${loader_path_env}