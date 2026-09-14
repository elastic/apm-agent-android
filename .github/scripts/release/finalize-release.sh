#!/usr/bin/env bash
#
# Finish a release after the artifacts are published.
#
# Usage: finalize-release.sh <release-sha> <release-version> <base-ref> <create-tag:true|false>
#
# Called by publish-release.yml once Buildkite has published. In order:
# create the `vX.Y.Z` tag at the merged commit, create the GitHub Release
# from the release-notes section, commit the next `-SNAPSHOT` version on the
# `releasing/X.Y.Z` branch, and open the PR from that branch into `main`.
#
# Every step first checks whether its result already exists and skips it if
# so, so a failed run can be re-run and picks up where it stopped without
# repeating anything. `create-tag` is the guard's verdict: `false` means the
# tag already exists at the merged commit.

set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "Usage: finalize-release.sh <release-sha> <release-version> <base-ref> <create-tag:true|false>" >&2
  exit 2
fi

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
release_sha=$1
release_version=$2
base_ref=$3
create_tag=$4
repository=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required}
work_dir=build/release-automation
tag="v$release_version"
tag_url="${GITHUB_SERVER_URL:-https://github.com}/$repository/releases/tag/$tag"
docs_url="https://www.elastic.co/docs/release-notes/edot/sdks/android#elastic-apm-android-agent-${release_version//./}-release-notes"

if [[ $base_ref != "releasing/$release_version" ]]; then
  echo "Unexpected release branch $base_ref for version $release_version." >&2
  exit 1
fi

mkdir -p "$work_dir"
git fetch --quiet --tags origin main "$base_ref"

# The guard already validated an existing tag; create it only when absent.
if [[ $create_tag == true ]]; then
  git tag "$tag" "$release_sha"
  git push origin "refs/tags/$tag"
fi

# The GitHub Release body is the release-notes section for this version,
# read from the released commit, plus the link to the published docs.
if gh release view "$tag" --repo "$repository" >/dev/null 2>&1; then
  release_url=$(gh release view "$tag" --repo "$repository" --json url --jq .url)
else
  release_body="$work_dir/github-release.md"
  git show "$release_sha:docs/release-notes/index.md" \
    | awk -v version="$release_version" '
        $0 ~ "^## " version " " { capture = 1 }
        capture && $0 ~ "^## " && $0 !~ "^## " version " " { exit }
        capture { print }
      ' >"$release_body"
  if [[ ! -s $release_body ]]; then
    echo "Could not extract release notes for $release_version." >&2
    exit 1
  fi
  {
    printf '\n'
    printf '[Published documentation](%s)\n' "$docs_url"
  } >>"$release_body"
  release_url=$(
    gh release create "$tag" \
      --repo "$repository" \
      --target "$release_sha" \
      --title "EDOT Android $release_version" \
      --notes-file "$release_body"
  )
fi

# Move the release branch to the next development version. The tag stays on
# the merged commit; this commit comes right after it. The branch must still
# point at the merged commit, or at the bump commit from a previous run of
# this script: anything else means something was pushed after the merge and
# would reach main without having been published, so stop.
next_development=$("$script_dir/version.sh" next-development "$release_version")
branch_tip=$(git rev-parse "origin/$base_ref")
if [[ $branch_tip == "$release_sha" ]]; then
  git switch -C "$base_ref" "$release_sha"
  sed -i.bak "s/^version=.*/version=$next_development/" gradle.properties
  rm gradle.properties.bak
  git add -- gradle.properties
  git commit -m "Prepare for the next release"
  git push origin "$base_ref"
elif [[ $(git rev-parse "$branch_tip^") == "$release_sha" \
  && $(git diff --name-only "$release_sha" "$branch_tip") == gradle.properties \
  && $(git show "$branch_tip:gradle.properties" | sed -n 's/^version=//p') == "$next_development" ]]; then
  # Exactly one commit on top of the merged one, touching only the version
  # file: that is this script's own bump from a previous run.
  echo "$base_ref already carries the next development version."
else
  echo "$base_ref moved after the merge: its tip is $branch_tip, expected $release_sha or its bump commit. Not opening a release PR from unpublished changes." >&2
  exit 1
fi

# The PR into main carries the release changes and the version bump. An
# operator merges it; the bot never does. Reuse an open or merged PR; a PR
# that was closed without merging does not count, so a rerun opens a new one.
main_pr=
for state in open merged; do
  main_pr=$(
    gh pr list \
      --repo "$repository" \
      --state "$state" \
      --base main \
      --head "$base_ref" \
      --limit 1 \
      --json url \
      --jq '.[0].url // empty'
  )
  [[ -z $main_pr ]] || break
done
if [[ -z $main_pr ]]; then
  body_file="$work_dir/main-pull-request-body.md"
  {
    printf 'EDOT Android %s is published: [%s](%s), [GitHub Release](%s).\n\n' "$release_version" "$tag" "$tag_url" "$release_url"
    printf 'This pull request brings the release changes into `main` and sets the development version to `%s`. Merge it to finish the release.\n' "$next_development"
  } >"$body_file"
  main_pr=$(
    gh pr create \
      --repo "$repository" \
      --base main \
      --head "$base_ref" \
      --title "Release $release_version" \
      --body-file "$body_file"
  )
fi

if [[ -n ${GITHUB_OUTPUT:-} ]]; then
  {
    echo "release_url=$release_url"
    echo "main_pull_request_url=$main_pr"
  } >>"$GITHUB_OUTPUT"
fi
