#!/usr/bin/env bash
#
# Buildkite release step for EDOT Android.
#
# Publishes the SDK artifacts to Maven Central and the Gradle plugins to the
# Gradle Plugin Portal, then archives every built JAR and AAR so the GitHub
# workflow can attest them. The GitHub "Publish release" workflow starts this
# pipeline after an operator merges the preparation PR. The "Release dry
# run" workflow starts it with dry_run=true to exercise the build without
# publishing anything.
#
# Inputs, passed as Buildkite environment variables by those workflows:
#   release_sha       the merged commit; must be what Buildkite checked out
#   release_version   the version being released, for example 1.10.0
#   target_specifier  all | mavenCentral | pluginPortal
#   dry_run           true | false (default true; only false publishes)
#   TARBALL_FILE      archive name for the built artifacts (default dist.tar)
#
# Credentials are injected by the Buildkite pipeline:
#   Maven Central: ORG_GRADLE_PROJECT_mavenCentralUsername,
#                  ORG_GRADLE_PROJECT_mavenCentralPassword
#   Signing:       SECRING_ASC (armored private key), KEYPASS_SECRET (its
#                  passphrase), read by the build's publishing plugin
#   Plugin Portal: PLUGIN_PORTAL_KEY, PLUGIN_PORTAL_SECRET
#
# Publishing cannot be undone, and both registries reject a second upload of
# an existing version. The script therefore publishes first and, when an
# upload fails, checks the registry to tell "already published" from a real
# failure. That way a rerun after a partial failure finishes the release
# instead of failing on what already went through.

set -euo pipefail

: "${release_sha:?release_sha is required}"
: "${target_specifier:?target_specifier is required}"
# A trigger that does not say otherwise is a dry run: publishing is the one
# outcome that must be asked for explicitly.
dry_run=${dry_run:-true}
if [[ $dry_run == false ]]; then
  : "${release_version:?release_version is required for a real publish}"
else
  release_version=${release_version:-$(sed -n 's/^version=//p' gradle.properties)}
fi

# An unknown target would skip both publish blocks and still exit
# successfully, so the release would be tagged without being published.
case $target_specifier in
  all | mavenCentral | pluginPortal) ;;
  *)
    echo "Unsupported target_specifier '$target_specifier'; expected all, mavenCentral, or pluginPortal." >&2
    exit 1
    ;;
esac
case $dry_run in
  true | false) ;;
  *)
    echo "Unsupported dry_run '$dry_run'; expected true or false." >&2
    exit 1
    ;;
esac

echo "--- Verify the checked-out commit"
# The workflow asks Buildkite to build one specific commit: the merge commit
# of the preparation PR. Refuse anything else, so a branch that moved after
# the merge can never be released.
if [[ $(git rev-parse HEAD) != "$release_sha" ]]; then
  echo "Buildkite checked out $(git rev-parse HEAD); expected release SHA $release_sha." >&2
  exit 1
fi

# A real publish must name the version it publishes, and that version must be
# the one the checked-out commit builds. This stops a manual trigger from
# publishing a -SNAPSHOT or a version other than the prepared one.
if [[ $dry_run == false ]]; then
  checked_out_version=$(sed -n 's/^version=//p' gradle.properties)
  if [[ ! $release_version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "release_version '$release_version' is not a release version; a real publish requires X.Y.Z." >&2
    exit 1
  fi
  if [[ $checked_out_version != "$release_version" ]]; then
    echo "gradle.properties at $release_sha has version $checked_out_version; expected $release_version." >&2
    exit 1
  fi
fi

common_gradle_deploy_params=(-Prelease=true --stacktrace)

if [[ $dry_run == false ]]; then
  echo "--- Verify the publishing credentials"
  # Stop before uploading anything if a credential for a selected target is
  # missing, so a missing Portal secret cannot leave Maven Central published
  # and the Portal not. Gradle reads the ORG_GRADLE_PROJECT_* variables as
  # project properties; the build's publishing plugin reads SECRING_ASC and
  # KEYPASS_SECRET to sign the artifacts.
  if [[ $target_specifier == all || $target_specifier == mavenCentral ]]; then
    : "${ORG_GRADLE_PROJECT_mavenCentralUsername:?missing Maven Central credential}"
    : "${ORG_GRADLE_PROJECT_mavenCentralPassword:?missing Maven Central credential}"
    : "${SECRING_ASC:?missing signing key}"
    : "${KEYPASS_SECRET:?missing signing key passphrase}"
  fi
  if [[ $target_specifier == all || $target_specifier == pluginPortal ]]; then
    : "${PLUGIN_PORTAL_KEY:?missing Gradle Plugin Portal key}"
    : "${PLUGIN_PORTAL_SECRET:?missing Gradle Plugin Portal secret}"
  fi
fi

# True when the URL answers HTTP 200 to a HEAD request. Only 200 counts: the
# Gradle Plugin Portal answers a missing version with a 303 redirect, which
# `curl -f` would accept as success.
is_public() {
  [[ $(curl -s -o /dev/null -w '%{http_code}' -I "$1") == 200 ]]
}

if [[ $target_specifier == all || $target_specifier == mavenCentral ]]; then
  if [[ $dry_run == true ]]; then
    echo "--- Release the binaries to Maven Central :package: (dry-run)"
    ./gradlew assemble
  else
    echo "--- Release the binaries to Maven Central"
    if ! ./gradlew publishAndReleaseElasticToMavenCentral "${common_gradle_deploy_params[@]}"; then
      # All modules go to Maven Central in one deployment that is released or
      # dropped as a whole, so one module's POM tells whether this version is
      # already public.
      central_pom="https://repo1.maven.org/maven2/co/elastic/otel/android/agent-sdk/$release_version/agent-sdk-$release_version.pom"
      if is_public "$central_pom"; then
        echo "Maven Central already has $release_version; nothing to publish."
      else
        echo "Publishing to Maven Central failed and $release_version is not public." >&2
        exit 1
      fi
    fi
  fi
fi

if [[ $target_specifier == all || $target_specifier == pluginPortal ]]; then
  if [[ $dry_run == true ]]; then
    echo "--- Release the binaries to the Gradle Plugin Portal :package: (dry-run)"
    ./gradlew assemble
  else
    echo "--- Release the binaries to the Gradle Plugin Portal"
    # Unlike Maven Central, each plugin project uploads on its own, so a
    # failed run can leave some projects published and others not. Publish
    # project by project, keep going when one fails so the others get
    # through, and treat a failure as success when the Portal already has
    # every plugin of that project. Report what is still missing at the end
    # so a rerun can finish it.
    #
    # The project is the finest retry unit there is: `publishPlugins` is one
    # task per project and the Portal plugin offers no per-plugin publish, so
    # a project that declares several plugins (agent-plugin declares two)
    # cannot be retried plugin by plugin. If such a project ever ends up with
    # some plugins public and others missing, the marker check below reports
    # exactly which ones, and a person finishes it. This is an accepted
    # trade-off, not an oversight.
    plugin_list=$(./gradlew -q listPublishedGradlePlugins)
    failed_projects=()
    for project_path in $(awk '{print $1}' <<<"$plugin_list" | sort -u); do
      # The credentials go on the command line here. Make sure tracing is off
      # so they can never be echoed to the log, even if someone enables
      # `set -x` above while debugging.
      set +x
      if ./gradlew "$project_path:publishPlugins" \
        "-Pgradle.publish.key=$PLUGIN_PORTAL_KEY" \
        "-Pgradle.publish.secret=$PLUGIN_PORTAL_SECRET" \
        "${common_gradle_deploy_params[@]}"; then
        continue
      fi
      already_public=true
      while read -r _ plugin_id; do
        marker="https://plugins.gradle.org/m2/${plugin_id//.//}/$plugin_id.gradle.plugin/$release_version/$plugin_id.gradle.plugin-$release_version.pom"
        if ! is_public "$marker"; then
          already_public=false
          echo "$plugin_id $release_version is not on the Gradle Plugin Portal." >&2
        fi
      done < <(awk -v path="$project_path" '$1 == path' <<<"$plugin_list")
      if [[ $already_public == true ]]; then
        echo "$project_path is already on the Gradle Plugin Portal; nothing to publish."
      else
        echo "$project_path failed to publish; continuing with the remaining plugins." >&2
        failed_projects+=("$project_path")
      fi
    done
    if ((${#failed_projects[@]} != 0)); then
      echo "Gradle Plugin Portal publication is incomplete for: ${failed_projects[*]}. Rerun the release to retry what is still missing." >&2
      exit 1
    fi
  fi
fi

echo "--- Archive the build folders with jar/aar files"
# Make sure the artifacts to attest exist even when nothing was published;
# assemble is a no-op when the publish tasks already built them. The GitHub
# workflow downloads this archive and attests every file in it.
./gradlew assemble
find . -type d -name build -exec find {} \( -name '*.jar' -o -name '*.aar' \) -print0 \; \
  | xargs -0 tar -cvf "${TARBALL_FILE:-dist.tar}"
