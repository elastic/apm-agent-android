#!/usr/bin/env bash
## This script run the release given the different environment variables
##  branch_specifier
##  target_specifier
##  version_override_specifier
##  WORKSPACE
##  VAULT_ROLE_ID
##  VAULT_SECRET_ID

set -e

## Stage 0. Prepare vault context to access the secrets
export VAULT_ROLE_ID=$(vault read -field=role-id secret/ci/elastic-observability-robots-playground/internal-ci-approle)
export VAULT_SECRET_ID=$(vault read -field=secret-id secret/ci/elastic-observability-robots-playground/internal-ci-approle)
export VAULT_ADDR=$(vault read -field=vault-url secret/ci/elastic-observability-robots-playground/internal-ci-approle)

## Stage 1. Prepare context

# Avoid detached HEAD since the release plugin requires to be on a branch
git checkout -f ${branch_specifier}
# Prepare a secure temp folder not shared between other jobs to store the key ring
export TMP_WORKSPACE=$WORKSPACE"@tmp"
export KEY_FILE=$TMP_WORKSPACE"/private.key"
# Secure home for our keyring
export GNUPGHOME=$TMP_WORKSPACE"/keyring"
mkdir -p $GNUPGHOME
chmod -R 700 $TMP_WORKSPACE
# Make sure we delete this folder before leaving even in case of failure
clean_up () {
  ARG=$?
  echo "Deleting tmp workspace"
  rm -rf $TMP_WORKSPACE
  echo "done"
  exit $ARG
}
trap clean_up EXIT

## Stage 2. Prepare secrets context
# Retrieve the secrets we are going to use in this job
set +x
export VAULT_TOKEN=$(vault write -field=token auth/approle/login role_id="$VAULT_ROLE_ID" secret_id="$VAULT_SECRET_ID")
# Nexus credentials
export ORG_GRADLE_PROJECT_sonatypeUsername=$(vault read -field=username secret/release/nexus)
export ORG_GRADLE_PROJECT_sonatypePassword=$(vault read -field=password secret/release/nexus)

# Gradle Plugin portal credentials
export PLUGIN_PORTAL_KEY=$(vault read secret/release/gradle-plugin-portal -format=json  | jq -r .data.key)
export PLUGIN_PORTAL_SECRET=$(vault read secret/release/gradle-plugin-portal -format=json  | jq -r .data.secret)

# Signing keys
vault read -field=key secret/release/signing >$KEY_FILE
export KEYPASS=$(vault read -field=passphrase secret/release/signing)
export KEY_ID=D88E42B4

# Import the key into the keyring
echo $KEYPASS | gpg --batch --import $KEY_FILE

# Export secring
export SECRING_FILE=$GNUPGHOME"/secring.gpg"
gpg --pinentry-mode=loopback --passphrase $KEYPASS --export-secret-key $KEY_ID > $SECRING_FILE
set -x
# Configure the committer since the maven release requires to push changes to GitHub
# This will help with the SLSA requirements.
git config --global user.email "infra-root+apmmachine@elastic.co"
git config --global user.name "apmmachine"

## Stage 3. Prepare Android SDK dependency

# Configure Android SDK using the script
./install-android-sdk.sh
export PATH=${PATH}:$PWD/.android-sdk/tools/bin/
export ANDROID_HOME=$PWD/.android-sdk

## Stage 4. Run release
## TODO: let's stop here so we can test things work nicely until here
exit 0

set +x
# Setting up common deploy params in env var
export COMMON_GRADLE_SIGNING_PARAMS="-Psigning.secretKeyRingFile=$SECRING_FILE -Psigning.password=$KEYPASS -Psigning.keyId=$KEY_ID"
export COMMON_GRADLE_CONFIG_PARAMS="-Prelease=true -Pversion_override=${version_override_specifier}"
export COMMON_GRADLE_DEPLOY_PARAMS="$COMMON_GRADLE_SIGNING_PARAMS $COMMON_GRADLE_CONFIG_PARAMS"

if [ ${target_specifier} == 'all' ] || [ ${target_specifier} == 'mavenCentral' ]
then
  # Release the binaries to Maven Central
  ./gradlew publishElasticPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository $COMMON_GRADLE_DEPLOY_PARAMS
fi

if [ ${target_specifier} == 'all' ] || [ ${target_specifier} == 'pluginPortal' ]
then
  # Release the binaries to the Gradle Plugin portal
  ./gradlew publishPlugins -Pgradle.publish.key=$PLUGIN_PORTAL_KEY -Pgradle.publish.secret=$PLUGIN_PORTAL_SECRET $COMMON_GRADLE_DEPLOY_PARAMS
fi
set -x

# Running post deploy process
./gradlew postDeploy $COMMON_GRADLE_CONFIG_PARAMS
