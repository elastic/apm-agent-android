#!/usr/bin/env bash
#
# Prepare a release: create the release branches and open the preparation PR.
#
# Called by prepare-release.yml on `main`. Environment inputs:
#   RELEASE_NOTES   the release-note JSON authored by the operator
#   BUMP            minor (default) | major
#
# Derives the release version from the highest `vX.Y.Z` tag and the bump,
# after checking that gradle.properties holds the expected `-SNAPSHOT`
# version. Then pushes `releasing/X.Y.Z`, an unchanged copy of the
# dispatched commit that serves as the PR base, and commits once on
# `prepare/X.Y.Z`: the version, the rendered release-notes section, the
# documentation `applies_to` versions on a major bump, and the regenerated
# NOTICE files. Opens the PR from `prepare/X.Y.Z` into `releasing/X.Y.Z`.
# Merging that PR publishes the release.
#
# All checks run before anything is pushed. If no PR was merged since the
# last release, the script exits successfully without creating anything.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
work_dir=build/release-automation
release_notes=${RELEASE_NOTES:?RELEASE_NOTES is required}
bump=${BUMP:-minor}
repository=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required}
release_ref=${GITHUB_SHA:-HEAD}

mkdir -p "$work_dir"
previous_tag=$("$script_dir/version.sh" highest-tag)
development_version=$(sed -n 's/^version=//p' gradle.properties)
release_version=$(
  "$script_dir/version.sh" release-version "$previous_tag" "$development_version" "$bump"
)
release_branch="releasing/$release_version"
prepare_branch="prepare/$release_version"

# Only one release can be in flight. A releasing branch exists from the
# moment preparation pushes it until the release PR into main is merged and
# the branch deleted.
existing_release_branches=$(git ls-remote --heads origin 'refs/heads/releasing/*' | awk '{print $2}')
if [[ -n $existing_release_branches ]]; then
  echo "A release is already in flight. Finish it, or delete its branches if it was abandoned:" >&2
  sed 's|refs/heads/|  |' <<<"$existing_release_branches" >&2
  exit 1
fi

range_file="$work_dir/range.json"
"$script_dir/pr-range.sh" "$release_ref" "$previous_tag" >"$range_file"
if [[ $(jq '.pullRequests | length' "$range_file") -eq 0 ]]; then
  message="No contributing pull requests remain after excluding release bookkeeping; preparation is a no-op."
  echo "$message"
  if [[ -n ${GITHUB_STEP_SUMMARY:-} ]]; then
    printf '### Prepare release\n\n%s\n' "$message" >>"$GITHUB_STEP_SUMMARY"
  fi
  exit 0
fi

notes_file="$work_dir/release-notes.json"
rendered_file="$work_dir/release-notes.md"
printf '%s\n' "$release_notes" | jq . >"$notes_file"
"$script_dir/render-release-notes.sh" "$notes_file" "$release_version" >"$rendered_file"

marker='% next_release_notes'
if [[ $(grep -Fxc "$marker" docs/release-notes/index.md) -ne 1 ]]; then
  echo "Expected exactly one '$marker' marker in docs/release-notes/index.md." >&2
  exit 1
fi
if grep -Eq "^## $release_version " docs/release-notes/index.md; then
  echo "Release notes for $release_version already exist." >&2
  exit 1
fi

sed -i.bak "s/^version=.*/version=$release_version/" gradle.properties
rm gradle.properties.bak

# Documentation `applies_to` entries name the version a feature ships in. They
# were written against the development version; on a major bump that version
# is never released, so rewrite them to the release version.
development_base=${development_version%-SNAPSHOT}
if [[ $release_version != "$development_base" ]]; then
  while IFS= read -r -d '' file; do
    OLD_VERSION=$development_base NEW_VERSION=$release_version perl -0pi -e '
      s/(edot_android:\s+[a-z_]+\s+)\Q$ENV{OLD_VERSION}\E\b/$1$ENV{NEW_VERSION}/g
    ' "$file"
  done < <(find docs -type f -name '*.md' -print0)
fi

updated_notes="$work_dir/release-notes-index.md"
awk -v marker="$marker" -v notes="$rendered_file" '
  { print }
  $0 == marker {
    while ((getline line < notes) > 0) {
      print line
    }
    close(notes)
  }
' docs/release-notes/index.md >"$updated_notes"
mv "$updated_notes" docs/release-notes/index.md

if ! ./gradlew createNoticeFile; then
  echo "NOTICE generation failed. Update manual_licenses_map.txt for dependencies whose licenses cannot be resolved, then dispatch again." >&2
  exit 1
fi

# Everything checked out. From here on the script writes to GitHub.
# The base branch is the dispatched commit, untouched; the release changes
# go on the prepare branch so the PR diff shows exactly what the release
# adds.
git branch "$release_branch" "$release_ref"
git push origin "refs/heads/$release_branch"
git switch -c "$prepare_branch"
# Stage additions too: a module that gained its first NOTICE since the last
# release must ship it. Only the paths this script changes are staged.
git add -A -- gradle.properties docs NOTICE ':(glob)**/src/main/resources/META-INF/NOTICE'
git commit -m "Prepare release $release_version"
git push --set-upstream origin "$prepare_branch"

summary=$(
  jq -r \
    '.pullRequests[]
     | "- [#\(.number)](\(.url)) \(.title)"' \
    "$range_file"
)
body_file="$work_dir/pull-request-body.md"
{
  printf 'Prepare EDOT Android %s.\n\n' "$release_version"
  printf -- '- Bump: `%s`\n' "$bump"
  printf -- '- Previous release: `%s`\n' "$previous_tag"
  printf -- '- Included pull requests:\n%s\n\n' "$summary"
  printf '> Merging this pull request publishes %s to Maven Central and the Gradle Plugin Portal and tags the merge commit. Review it, then merge when the checks are green. The automation then opens a second pull request from `%s` into `main`.\n' "$release_version" "$release_branch"
} >"$body_file"

pr_url=$(
  gh pr create \
    --repo "$repository" \
    --base "$release_branch" \
    --head "$prepare_branch" \
    --title "Prepare release $release_version" \
    --body-file "$body_file"
)

if [[ -n ${GITHUB_STEP_SUMMARY:-} ]]; then
  {
    printf '### Prepared EDOT Android %s\n\n' "$release_version"
    printf 'Preparation pull request: %s\n\n' "$pr_url"
    printf '%s\n' "$summary"
  } >>"$GITHUB_STEP_SUMMARY"
fi
