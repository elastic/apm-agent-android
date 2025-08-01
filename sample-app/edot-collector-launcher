#!/bin/sh
set -e

BASEDIR=$(dirname "$0")
EDOT_BUILD_DIR="$BASEDIR/edot-collector/build"

"$BASEDIR/gradlew" :edot-collector:prepareEdotCollector

exec "$EDOT_BUILD_DIR/bin/otelcol" --config "$EDOT_BUILD_DIR/configuration/edot-configuration.yml"
