FROM openjdk:11

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# can be passed during Docker build as build time environment for github branch to pickup configuration from | spring profiles active | config server URL | glowroot and artifactory URL respectively .
ARG spring_config_label 
ARG active_profile
ARG spring_config_url
ARG plugin_name

# environment variable to pass active profile such as DEV, QA etc at docker runtime | github branch to pickup configuration from, at docker runtime | spring configuration url, at docker runtime | glowroot, at docker runtime | artifactory url, at docker runtime and iam_adapter url, at docker runtime, respectively.
ENV active_profile_env=${active_profile}
ENV spring_config_label_env=${spring_config_label}
ENV spring_config_url_env=${spring_config_url}
ENV plugin_name_env=${plugin_name}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# set working directory for the user
WORKDIR /home/${container_user}

ENV work_dir=/home/${container_user}

ARG loader_path=${work_dir}/additional_jars/

ENV loader_path_env=${loader_path}

ARG plugins_path=${work_dir}/plugins

ENV plugins_path_env=${plugins_path}

ADD configure_start.sh configure_start.sh

COPY ./target/signup-service-*.jar signup-service.jar

## install packages, create user and change permissions of file inside working dir
RUN apt-get -y update \
&& apt-get install -y unzip file sudo \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
&& adduser ${container_user} sudo \
&& mkdir -p ${loader_path} \
&& mkdir -p ${plugins_path} \
&& chmod +x configure_start.sh \
&& chown -R ${container_user}:${container_user} /home/${container_user}

# copy all files under target/esignet-plugins to the plugins folder
COPY ./target/esignet-mock-plugin.jar ./target/mosip-identity-plugin.jar ${plugins_path}
COPY ./target/kernel-auth-adapter-lite.jar ${loader_path}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

EXPOSE 8089

ENTRYPOINT [ "./configure_start.sh" ]
CMD java -jar -Dloader.path="${loader_path_env}" -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" signup-service.jar
