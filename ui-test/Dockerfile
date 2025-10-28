# --- Base image ---
FROM openjdk:21-slim

# Use root for package installation
USER root

# Set display for headless mode (used by Selenium / Chrome)
ENV DISPLAY=:99

# --- Install Google Chrome & Chromedriver ---
# Note: chromium-driver installs Chromium dependencies; avoids missing lib issues
RUN apt-get update && \
    apt-get install -y wget curl unzip xvfb libxi6 libgconf-2-4 chromium chromium-driver

# --- Install kubectl (latest stable) ---
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    chmod +x kubectl && \
    mv kubectl /usr/local/bin/kubectl

# --- Build-time metadata arguments ---
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# --- Metadata labels (for CI/CD traceability) ---
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# --- User configuration ---
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
ENV work_dir=/home/${container_user}

# Create user and group
RUN groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} -d ${work_dir}

# --- Create necessary directories ---
RUN mkdir -p ${work_dir}/target \
    && mkdir -p ${work_dir}/src/test/resources \
    && mkdir -p ${work_dir}/test-output/SparkReport \
    && mkdir -p ${work_dir}/screenshots \
    && mkdir -p ${work_dir}/featurefiles \
    && mkdir -p ${work_dir}/src/main/resources \
    && mkdir -p ${work_dir}/src/main/java/utils \
    && mkdir -p ${work_dir}/testNgXmlFiles \
    && chmod -R 777 ${work_dir}

# --- Copy application JAR ---
COPY --chmod=664 ./target/uitest-signup-1.3.0-SNAPSHOT.jar ${work_dir}/target/uitest-signup-1.3.0-SNAPSHOT.jar

# --- Copy scripts and test files ---
COPY --chmod=771 ./entrypoint.sh ${work_dir}/entrypoint.sh
COPY ./src/main/resources/featurefiles/ ${work_dir}/featurefiles/
COPY ./src/main/resources/ ${work_dir}/src/main/resources/
COPY ./src/main/resources/extent.properties ${work_dir}/src/test/resources/extent.properties
COPY ./src/main/java/utils/ ${work_dir}/src/main/java/utils/
COPY ./testNgXmlFiles/ ${work_dir}/testNgXmlFiles/
COPY ./browserstack.yml ${work_dir}/

# --- Final permission fix ---
RUN chmod -R 777 ${work_dir}

# --- Working directory ---
WORKDIR ${work_dir}

# --- Run as root (can be changed to mosip if permissions are correct) ---
USER root

# --- Entrypoint ---
ENTRYPOINT ["/home/mosip/entrypoint.sh"]
