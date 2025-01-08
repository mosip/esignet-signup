## Signup service with plugins docker

This directory contains files required to build the Signup docker with default plugins preloaded. All the plugins
available under [esignet-plugins](https://github.com/mosip/esignet-plugins) repository is included in the "signup-with-plugins" docker image.

Based on the configured plugin name during the runtime, corresponding plugin jar will be copied to the Signup service 
classpath from the plugins directory in the docker container.
For example, "plugin_name_env" environment variable is set to "esignet-mock-plugin.jar", then "esignet-mock-plugin.jar" is copied
to loader_path in the signup service container. After successful copy Signup service is started.

"signup-with-plugins" docker image is created with "signup-service" base image. The base image can also be directly used to start the signup
service. Mount external directory with the plugin onto "/home/mosip/plugins" directory in the container and finally set "plugin_name_env" environment variable. 
With this setup, signup service should get started with the configured plugin.

## License
This project is licensed under the terms of [Mozilla Public License 2.0](../LICENSE).