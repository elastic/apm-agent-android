#!/usr/bin/env bash
## This script runs the release given the different environment variables
##  branch_specifier
##  target_specifier
##  dry_run
##
##  NOTE: *_SECRET env variables are masked, hence if you'd like to avoid any
##        surprises please use the suffix _SECRET for those values that contain
##        any sensitive data. Buildkite can mask those values automatically

set -e

echo "--- Prepare release context"
# Avoid detached HEAD since the release plugin requires to be on a branch
git checkout -f "${branch_specifier}"

set +x
# Setting up common deploy params in env var
export COMMON_GRADLE_DEPLOY_PARAMS="-Prelease=true --stacktrace"

if [[ "$target_specifier" == "all" ||  "$target_specifier" == "mavenCentral" ]]; then
  if [[ "$dry_run" == "false" ]] ; then
    echo "--- Release the binaries to Maven Central"
    ./gradlew publishElasticPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository $COMMON_GRADLE_DEPLOY_PARAMS
  else
    echo "--- Release the binaries to Maven Central :package: (dry-run)"
    ./gradlew assemble
  fi
fi

if [[ "$target_specifier" == "all" ||  "$target_specifier" == "pluginPortal" ]]; then
  if [[ "$dry_run" == "false" ]] ; then
    echo "--- Release the binaries to the Gradle Plugin portal"
    ./gradlew publishPlugins -Pgradle.publish.key=$PLUGIN_PORTAL_KEY -Pgradle.publish.secret=$PLUGIN_PORTAL_SECRET $COMMON_GRADLE_DEPLOY_PARAMS
  else
    echo "--- Release the binaries to Gradle Plugin portal :package: (dry-run)"
    ./gradlew assemble
  fi
fi

echo "--- Archive the build folders with jar/aar files"
find . -type d -name build -exec find {} \( -name '*.jar' -o -name '*.aar' \) -print0 \; | xargs -0 tar -cvf "${TARBALL_FILE:-dist.tar}"

set -x
