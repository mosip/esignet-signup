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

ENV nginx_dir=/usr/share/nginx
ENV work_dir=${nginx_dir}/html

# set working directory for the user
WORKDIR /home/signup_ui

COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
# copy build files to nginx html directory
COPY --from=build_signup_ui /build  ${work_dir}

EXPOSE 3000

CMD echo "starting nginx" ; \
    nginx ; \
    sleep infinity
