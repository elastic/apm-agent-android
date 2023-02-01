#!/usr/bin/env bash
## This script runs the release given the different environment variables
##  branch_specifier
##  target_specifier
##  version_override_specifier

set -ex


if [[ "$target_specifier" == "all" ||  "$target_specifier" == "mavenCentral" ]]; then
  echo "--- Release the binaries to Maven Central"
fi

if [[ "$target_specifier" == "all" ||  "$target_specifier" == "pluginPortal" ]]; then
  echo "--- Release the binaries to the Gradle Plugin portal"
fi

exit 0

echo "--- Prepare vault context"
set +x
VAULT_ROLE_ID=$(vault read -field=role-id secret/ci/elastic-observability-robots-playground/internal-ci-approle)
export VAULT_ROLE_ID
VAULT_SECRET_ID=$(vault read -field=secret-id secret/ci/elastic-observability-robots-playground/internal-ci-approle)
export VAULT_SECRET_ID
VAULT_ADDR=$(vault read -field=vault-url secret/ci/elastic-observability-robots-playground/internal-ci-approle)
export VAULT_ADDR

# Delete the vault specific accessing the ci vault
unset VAULT_TOKEN

echo "--- Prepare release context"
# Avoid detached HEAD since the release plugin requires to be on a branch
git checkout -f "${branch_specifier}"
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
  echo "--- Deleting tmp workspace"
  rm -rf $TMP_WORKSPACE
  exit $ARG
}
trap clean_up EXIT

echo "--- Prepare keys context"
# Retrieve the secrets we are going to use in this job
set +x
VAULT_TOKEN=$(vault write -address="${VAULT_ADDR}" -field=token auth/approle/login role_id="$VAULT_ROLE_ID" secret_id="$VAULT_SECRET_ID")
export VAULT_TOKEN
# Nexus credentials
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
KEYPASS=$(vault read -field=passphrase secret/release/signing)
export KEYPASS
export KEY_ID=D88E42B4
unset VAULT_TOKEN

# Import the key into the keyring
echo "$KEYPASS" | gpg --batch --import "$KEY_FILE"

# Export secring
export SECRING_FILE=$GNUPGHOME"/secring.gpg"
gpg --pinentry-mode=loopback --passphrase "$KEYPASS" --export-secret-key $KEY_ID > "$SECRING_FILE"
set -x
# Configure the committer since the maven release requires to push changes to GitHub
# This will help with the SLSA requirements.
git config --global user.email "infra-root+apmmachine@elastic.co"
git config --global user.name "apmmachine"

echo "--- Install Android SDK"
# Configure Android SDK using the script
./install-android-sdk.sh
export PATH=${PATH}:$PWD/.android-sdk/tools/bin/
export ANDROID_HOME=$PWD/.android-sdk

set +x
# Setting up common deploy params in env var
export COMMON_GRADLE_SIGNING_PARAMS="-Psigning.secretKeyRingFile=$SECRING_FILE -Psigning.password=$KEYPASS -Psigning.keyId=$KEY_ID"
export COMMON_GRADLE_CONFIG_PARAMS="-Prelease=true -Pversion_override=${version_override_specifier}"
export COMMON_GRADLE_DEPLOY_PARAMS="$COMMON_GRADLE_SIGNING_PARAMS $COMMON_GRADLE_CONFIG_PARAMS"

set -x

echo "--- Running post deploy process"
echo "./gradlew postDeploy $COMMON_GRADLE_CONFIG_PARAMS"
