#!/usr/bin/env bash
## This script runs the release given the different environment variables
##  branch_specifier
##  target_specifier
##  version_override_specifier
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
export COMMON_GRADLE_SIGNING_PARAMS="-Psigning.secretKeyRingFile=$SECRING_FILE -Psigning.password=$KEYPASS_SECRET -Psigning.keyId=$KEY_ID_SECRET"
export COMMON_GRADLE_CONFIG_PARAMS="-Prelease=true -Pversion_override=${version_override_specifier}"
export COMMON_GRADLE_DEPLOY_PARAMS="$COMMON_GRADLE_SIGNING_PARAMS $COMMON_GRADLE_CONFIG_PARAMS"

if [[ "$target_specifier" == "all" ||  "$target_specifier" == "mavenCentral" ]]; then
  echo "--- Release the binaries to Maven Central"
  if [[ "$dry_run" == "true" ]] ; then
    echo './gradlew publishElasticPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository'
  else
    ./gradlew publishElasticPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository $COMMON_GRADLE_DEPLOY_PARAMS
  fi
fi

if [[ "$target_specifier" == "all" ||  "$target_specifier" == "pluginPortal" ]]; then
  echo "--- Release the binaries to the Gradle Plugin portal"
  if [[ "$dry_run" == "true" ]] ; then
    echo './gradlew publishPlugins'
  else
    ./gradlew publishPlugins -Pgradle.publish.key=$PLUGIN_PORTAL_KEY -Pgradle.publish.secret=$PLUGIN_PORTAL_SECRET $COMMON_GRADLE_DEPLOY_PARAMS
  fi
fi
set -x
