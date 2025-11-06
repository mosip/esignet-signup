# --- Base image ---
FROM eclipse-temurin:21-jre-alpine

# --- Build-time arguments (MUST be declared before LABEL) ---
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# --- Metadata labels (used by CI for validation) ---
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# --- Additional build-time args ---
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
ARG KUBECTL_VERSION=1.22.9

# --- Environment variables ---
ENV DISPLAY=:99
ENV work_dir=/home/${container_user}

# --- Install dependencies & setup user ---
RUN apk -q update && \
    apk add --no-cache wget curl unzip xvfb libxi chromium chromium-chromedriver bash shadow && \
    \
    addgroup -g ${container_user_gid} ${container_user_group} && \
    adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user} && \
    \
    curl -LO "https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl" && \
    chmod +x kubectl && mv kubectl /usr/local/bin/ && \
    \
    mkdir -p ${work_dir}/src/test/resources/ \
             ${work_dir}/test-output/SparkReport/ \
             ${work_dir}/screenshots/ && \
    chmod -R 775 ${work_dir}

# --- Copy project files with correct ownership ---
COPY --chown=${container_user_uid}:${container_user_gid} ./entrypoint.sh ${work_dir}/entrypoint.sh
COPY --chown=${container_user_uid}:${container_user_gid} ./target/*.jar ${work_dir}/
COPY --chown=${container_user_uid}:${container_user_gid} ./src/main/resources/featurefiles/ ${work_dir}/featurefiles/
COPY --chown=${container_user_uid}:${container_user_gid} ./src/main/resources/ ${work_dir}/src/main/resources/
COPY --chown=${container_user_uid}:${container_user_gid} ./src/main/resources/extent.properties ${work_dir}/src/test/resources/extent.properties
COPY --chown=${container_user_uid}:${container_user_gid} ./src/main/java/utils/ ${work_dir}/src/main/java/utils/
COPY --chown=${container_user_uid}:${container_user_gid} ./testNgXmlFiles/ ${work_dir}/testNgXmlFiles/
COPY --chown=${container_user_uid}:${container_user_gid} ./browserstack.yml ${work_dir}/

# --- Fix permissions for entrypoint and JARs ---
RUN chmod +x ${work_dir}/entrypoint.sh && \
    find ${work_dir} -name "*.jar" -exec chmod 644 {} \; && \
    chown -R ${container_user}:${container_user} ${work_dir};

# --- Switch to non-root user ---
USER ${container_user_uid}:${container_user_gid}

# --- Set working directory ---
WORKDIR ${work_dir}

# --- Default entrypoint ---
ENTRYPOINT ["bash", "entrypoint.sh"]
