FROM node:12.18.4-alpine as build_signup_ui

ARG signUpPublicUrl
ENV PUBLIC_URL=$signUpPublicUrl

COPY package*.json ./
RUN npm install
#Copy the working directory
COPY . ./
RUN npm run build

# multi stage build
FROM nginx

RUN apt-get -y update \
    && apt-get install -y curl npm wget unzip zip

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

ENV nginx_dir=/usr/share/nginx
ENV work_dir=${nginx_dir}/html
ENV i18n_path=${work_dir}/locales
ENV plugins_path=${nginx_dir}/html/plugins
ENV plugins_format=iife

# set working directory for the user
WORKDIR /home/${container_user}

# install packages and create user
RUN groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
    && mkdir -p /var/run/nginx /var/tmp/nginx ${i18n_path} ${plugins_path}  ${plugins_path}/temp \
    && chown -R ${container_user}:${container_user} /usr/share/nginx /var/run/nginx /var/tmp/nginx ${i18n_path} ${plugins_path} ${plugins_path}/temp

COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
# copy build files to nginx html directory
COPY --from=build_signup_ui /build  ${work_dir}

RUN chown -R ${container_user}:${container_user} /home/${container_user}

# change permissions of file inside working dir
RUN chown -R ${container_user}:${container_user} ${work_dir}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}
EXPOSE 3000

CMD echo "starting nginx" ; \
    nginx ; \
    sleep infinity
