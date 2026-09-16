#!/usr/bin/env bash
#
# Decide whether a merged preparation PR may publish a release.
#
# Usage: publish-guard.sh <base-ref> <head-ref> <merge-commit>
#
# publish-release.yml runs when a PR into a `releasing/*` branch is merged.
# GitHub's merge button already decided who may merge and whether the checks
# passed, so this script only confirms two things before anything
# irreversible happens:
#   - the merged commit is a prepared release: gradle.properties at the merge
#     commit holds a release version X.Y.Z, the base branch is
#     `releasing/X.Y.Z`, and the merged head is `prepare/X.Y.Z`, so no other
#     PR into the releasing branch can publish;
#   - whether it was already published: if tag vX.Y.Z exists it must point at
#     the merge commit, in which case a previous run published and failed
#     later, publication is skipped, and only the finalization steps run. A
#     tag at another commit stops the run.
# Outputs the release facts for the following workflow steps.

set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: publish-guard.sh <base-ref> <head-ref> <merge-commit>" >&2
  exit 2
fi

base_ref=$1
head_ref=$2
release_sha=$3

git fetch --quiet --tags origin "$release_sha"
version=$(git show "$release_sha:gradle.properties" | sed -n 's/^version=//p')
if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "The merged commit has version '$version', which is not a release version." >&2
  exit 1
fi
if [[ $base_ref != "releasing/$version" ]]; then
  echo "Expected base releasing/$version for version $version, found $base_ref." >&2
  exit 1
fi
if [[ $head_ref != "prepare/$version" ]]; then
  echo "Expected the merged head to be prepare/$version, found $head_ref." >&2
  exit 1
fi

# The tag is the record of what was published. If it exists at this commit,
# do not publish again; if it exists elsewhere, a different commit was
# released under this version and a person has to look.
tag="v$version"
publish_required=true
if git rev-parse -q --verify "refs/tags/$tag^{commit}" >/dev/null; then
  tag_sha=$(git rev-parse "refs/tags/$tag^{commit}")
  if [[ $tag_sha != "$release_sha" ]]; then
    echo "Tag $tag exists at $tag_sha, not at the merged commit $release_sha." >&2
    exit 1
  fi
  echo "Tag $tag already exists at $release_sha; publication is skipped."
  publish_required=false
fi

if [[ -n ${GITHUB_OUTPUT:-} ]]; then
  {
    echo "release_sha=$release_sha"
    echo "release_version=$version"
    echo "publish_required=$publish_required"
  } >>"$GITHUB_OUTPUT"
fi
