#!/bin/bash
# Run the server JAR (run from project root after: mvn clean package)
set -e
JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/server/target"
if [[ ! -f server-1.0-SNAPSHOT.jar ]]; then
  echo "JAR not found. Run from project root: mvn clean package"
  exit 1
fi
exec "$JAVA_HOME/bin/java" --module-path lib --add-modules javafx.controls -jar server-1.0-SNAPSHOT.jar
