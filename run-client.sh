#!/bin/bash
# Run the client JAR: ./run-client.sh [host] [port]
# Example: ./run-client.sh localhost 3000
# Run from project root after: mvn clean package
set -e
JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"
DIR="$(cd "$(dirname "$0")" && pwd)"
HOST="${1:-localhost}"
PORT="${2:-3000}"
cd "$DIR/client/target"
if [[ ! -f client-1.0-SNAPSHOT.jar ]]; then
  echo "JAR not found. Run from project root: mvn clean package"
  exit 1
fi
exec "$JAVA_HOME/bin/java" --module-path lib --add-modules javafx.controls -jar client-1.0-SNAPSHOT.jar "$HOST" "$PORT"
