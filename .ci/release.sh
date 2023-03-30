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

echo "--- Debug env variables"
env | sort

echo "--- Prepare vault context"
set +x
VAULT_ROLE_ID_SECRET=$(vault read -field=role-id secret/ci/elastic-apm-agent-android/internal-ci-approle)
export VAULT_ROLE_ID_SECRET
VAULT_SECRET_ID_SECRET=$(vault read -field=secret-id secret/ci/elastic-apm-agent-android/internal-ci-approle)
export VAULT_SECRET_ID_SECRET
VAULT_ADDR=$(vault read -field=vault-url secret/ci/elastic-apm-agent-android/internal-ci-approle)
export VAULT_ADDR

# Delete the vault specific accessing the ci vault
export VAULT_TOKEN_PREVIOUS=$VAULT_TOKEN
unset VAULT_TOKEN

echo "--- Prepare release context"
# Avoid detached HEAD since the release plugin requires to be on a branch
git checkout -f "${branch_specifier}"
# Prepare a secure temp folder not shared between other jobs to store the key ring
export TMP_WORKSPACE=/tmp/secured
export KEY_FILE=$TMP_WORKSPACE"/private.key"
# Secure home for our keyring
export GNUPGHOME=$TMP_WORKSPACE"/keyring"
mkdir -p $GNUPGHOME
chmod -R 700 $TMP_WORKSPACE
# Make sure we delete this folder before leaving even in case of failure
clean_up () {
  ARG=$?
  export VAULT_TOKEN=$VAULT_TOKEN_PREVIOUS
  echo "--- Deleting tmp workspace"
  rm -rf $TMP_WORKSPACE
  exit $ARG
}
trap clean_up EXIT

echo "--- Prepare keys context"
set +x
VAULT_TOKEN=$(vault write -field=token auth/approle/login role_id="$VAULT_ROLE_ID_SECRET" secret_id="$VAULT_SECRET_ID_SECRET")
export VAULT_TOKEN
# Nexus credentials (they cannot use the _SECRET pattern since they are in-memory based)
# See https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys
ORG_GRADLE_PROJECT_sonatypeUsername=$(vault read -field=username secret/release/nexus)
export ORG_GRADLE_PROJECT_sonatypeUsername
ORG_GRADLE_PROJECT_sonatypePassword=$(vault read -field=password secret/release/nexus)
export ORG_GRADLE_PROJECT_sonatypePassword

# Gradle Plugin portal credentials
PLUGIN_PORTAL_KEY=$(vault read secret/release/gradle-plugin-portal -format=json  | jq -r .data.key)
export PLUGIN_PORTAL_KEY
PLUGIN_PORTAL_SECRET=$(vault read secret/release/gradle-plugin-portal -format=json  | jq -r .data.secret)
export PLUGIN_PORTAL_SECRET

# Signing keys
vault read -field=key secret/release/signing >$KEY_FILE
KEYPASS_SECRET=$(vault read -field=passphrase secret/release/signing)
export KEYPASS_SECRET
export KEY_ID_SECRET=D88E42B4
unset VAULT_TOKEN

# Import the key into the keyring
echo "$KEYPASS_SECRET" | gpg --batch --import "$KEY_FILE"

# Export secring
export SECRING_FILE=$GNUPGHOME"/secring.gpg"
gpg --pinentry-mode=loopback --passphrase "$KEYPASS_SECRET" --export-secret-key $KEY_ID_SECRET > "$SECRING_FILE"
set -x
# Configure the committer since the maven release requires to push changes to GitHub
# This will help with the SLSA requirements.
git config --global user.email "infra-root+apmmachine@elastic.co"
git config --global user.name "apmmachine"

echo "--- Install JDK11"
JAVA_URL=https://jvm-catalog.elastic.co/jdk
JAVA_HOME=$(pwd)/.openjdk11
JAVA_PKG="$JAVA_URL/latest_openjdk_11_linux.tar.gz"
curl -L --output /tmp/jdk.tar.gz "$JAVA_PKG"; \
  mkdir -p "$JAVA_HOME"; \
  tar --extract --file /tmp/jdk.tar.gz --directory "$JAVA_HOME" --strip-components 1
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

echo "--- Install Android SDK"
# Configure Android SDK using the script
./install-android-sdk.sh
export PATH=${PATH}:$PWD/.android-sdk/tools/bin/
export ANDROID_HOME=$PWD/.android-sdk

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

echo "--- Running post deploy process"
if [[ "$dry_run" == "true" ]] ; then
  echo './gradlew postDeploy'
else
  ./gradlew postDeploy $COMMON_GRADLE_CONFIG_PARAMS
fi
