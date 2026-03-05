#!/bin/sh
# Maven wrapper - runs Maven using the wrapper JAR (downloads Maven if needed)
set -e
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"
export MAVEN_PROJECTBASEDIR="$BASE_DIR"
# Required for multi-module reactor when running from IDE or wrapper
export MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$BASE_DIR ${MAVEN_OPTS:-}"
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Maven wrapper JAR not found at $WRAPPER_JAR"
  exit 1
fi
exec "$JAVA_HOME/bin/java" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
