#!/bin/bash
set -e

echo "→ /home/mosip/"
ls -l /home/mosip/ || true
echo

echo "→ /home/mosip/featurefiles/"
ls -l /home/mosip/featurefiles/ || true
echo

echo "→ /home/mosip/src/test/resources/"
ls -l /home/mosip/src/test/resources/ || true
echo

echo "→ /home/mosip/test-output/SparkReport/"
ls -l /home/mosip/test-output/SparkReport/ || true
echo

echo "→ /home/mosip/src/test/resources/extent.properties"
ls -l /home/mosip/src/test/resources/extent.properties || true
echo

echo "→ /home/mosip/src/"
ls -l /home/mosip/src/ || true
echo

echo "→ /home/mosip/src/main/"
ls -l /home/mosip/src/main/ || true
echo

echo "→ /home/mosip/src/main/java/"
ls -l /home/mosip/src/main/java/ || true
echo

echo "→ /home/mosip/src/main/java/utils/"
ls -l /home/mosip/src/main/java/utils/ || true
echo

# Ensure test-output directory exists
mkdir -p /home/mosip/test-output

# Ensure ExtentReport.html exists (or create an empty file)
touch /home/mosip/test-output/ExtentReport.html

# Grant full access permissions
chmod -R 777 /home/mosip/test-output
chmod 777 /home/mosip/test-output/ExtentReport.html


java --version

echo "Running: java -jar -Denv.endpoint=\"$ENV_ENDPOINT\" uitest-signup-*.jar"
java -jar -Denv.endpoint="$ENV_ENDPOINT" uitest-signup-*.jar
