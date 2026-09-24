#!/usr/bin/env bash
#
# Finish a release after the artifacts are published.
#
# Usage: finalize-release.sh <release-sha> <release-version> <base-ref> <create-tag:true|false>
#
# Called by publish-release.yml once Buildkite has published. In order:
# create the `vX.Y.Z` tag at the merged commit, create the GitHub Release
# from the release-notes section, then finish according to the patch digit.
# A main release commits the next `-SNAPSHOT` on `releasing/X.Y.Z` and opens
# that branch's PR into `main`. A patch opens a notes-only PR and removes its
# ephemeral `patching/X.Y.Z` and `releasing/X.Y.Z` branches.
#
# Every step first checks whether its result already exists and skips it if
# so, so a failed run can be re-run and picks up where it stopped without
# repeating anything. `create-tag` is the guard's verdict: `false` means the
# tag already exists at the merged commit.
#
# Arguments:
#   release-sha       the merged commit, 40 hexadecimal characters
#   release-version   the version being released, X.Y.Z
#   base-ref          the release branch, releasing/X.Y.Z
#   create-tag        true to create the tag, false when it already exists
#
# Environment:
#   GITHUB_REPOSITORY  owner/repo of the release repository (required)
#   GH_TOKEN           GitHub App token with contents and pull-requests
#                      write access, used by every gh call (required)
#   GITHUB_SERVER_URL  GitHub base URL (default https://github.com)
#   GITHUB_OUTPUT      step output file; release_url and
#                      main_pull_request_url are written when it is set

set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "Usage: finalize-release.sh <release-sha> <release-version> <base-ref> <create-tag:true|false>" >&2
  exit 2
fi

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
release_sha=${1:?release-sha is required}
release_version=${2:?release-version is required}
base_ref=${3:?base-ref is required}
create_tag=${4:?create-tag is required}
repository=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required}
: "${GH_TOKEN:?GH_TOKEN is required}"

# Reject malformed values before anything is created; an empty or wrong
# argument must never reach the tag or release commands.
if [[ ! $release_sha =~ ^[0-9a-f]{40}$ ]]; then
  echo "release-sha '$release_sha' is not a full commit SHA." >&2
  exit 2
fi
if [[ ! $release_version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "release-version '$release_version' is not X.Y.Z." >&2
  exit 2
fi
if [[ $create_tag != true && $create_tag != false ]]; then
  echo "create-tag '$create_tag' must be true or false." >&2
  exit 2
fi
work_dir=build/release-automation
tag="v$release_version"
tag_url="${GITHUB_SERVER_URL:-https://github.com}/$repository/releases/tag/$tag"
docs_url="https://www.elastic.co/docs/release-notes/edot/sdks/android#elastic-apm-android-agent-${release_version//./}-release-notes"

if [[ $base_ref != "releasing/$release_version" ]]; then
  echo "Unexpected release branch $base_ref for version $release_version." >&2
  exit 1
fi

mkdir -p "$work_dir"
git fetch --quiet --tags origin main
if git ls-remote --exit-code --heads origin "refs/heads/$base_ref" >/dev/null 2>&1; then
  git fetch --quiet origin "$base_ref"
fi

find_main_pr() {
  local head_ref=$1
  local found=
  local state
  for state in open merged; do
    found=$(
      gh pr list \
        --repo "$repository" \
        --state "$state" \
        --base main \
        --head "$head_ref" \
        --limit 1 \
        --json url \
        --jq '.[0].url // empty'
    )
    [[ -z $found ]] || break
  done
  printf '%s\n' "$found"
}

# The guard already validated an existing tag; create it only when absent.
if [[ $create_tag == true ]]; then
  git tag "$tag" "$release_sha"
  git push origin "refs/tags/$tag"
fi

# Extract the released section once. It is shared by the GitHub Release and
# the patch notes-only PR.
release_section="$work_dir/release-section.md"
git show "$release_sha:docs/release-notes/index.md" \
  | awk -v version="$release_version" '
      $0 ~ "^## " version " " { capture = 1 }
      capture && $0 ~ "^## " && $0 !~ "^## " version " " { exit }
      capture { print }
    ' >"$release_section"
if [[ ! -s $release_section ]]; then
  echo "Could not extract release notes for $release_version." >&2
  exit 1
fi

# The GitHub Release body is the section plus the published-docs link.
if gh release view "$tag" --repo "$repository" >/dev/null 2>&1; then
  release_url=$(gh release view "$tag" --repo "$repository" --json url --jq .url)
else
  release_body="$work_dir/github-release.md"
  cp "$release_section" "$release_body"
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

patch_version=${release_version##*.}
if [[ $patch_version =~ ^0+$ ]]; then
  # Move the release branch to the next development version. The tag stays on
  # the merged commit; this commit comes right after it. The branch must still
  # point at the merged commit, or at this script's bump from a previous run.
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
    echo "$base_ref already carries the next development version."
  else
    echo "$base_ref moved after the merge: its tip is $branch_tip, expected $release_sha or its bump commit. Not opening a release PR from unpublished changes." >&2
    exit 1
  fi

  main_pr=$(find_main_pr "$base_ref")
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
else
  # The notes-only PR into main. A previous run may have pushed the notes
  # branch and failed before opening the PR: reuse that branch when its only
  # change over main is the index and it carries this section. Otherwise
  # build it from main, inserting the section above the first heading with a
  # lower version; the index is in descending order, so that is its place.
  notes_branch="patch-notes/$release_version"
  notes_index=docs/release-notes/index.md
  main_pr=$(find_main_pr "$notes_branch")
  if [[ -z $main_pr ]]; then
    notes_tip=$(git ls-remote --heads origin "refs/heads/$notes_branch" | awk '{print $1}')
    if [[ -n $notes_tip ]]; then
      git fetch --quiet origin "$notes_branch"
      if [[ $(git diff --name-only "$(git merge-base origin/main "$notes_tip")" "$notes_tip") != "$notes_index" ]] \
        || ! git show "$notes_tip:$notes_index" | grep -Eq "^## $release_version "; then
        echo "$notes_branch exists at $notes_tip but does not carry only the $release_version release notes. Not reusing it." >&2
        exit 1
      fi
      echo "$notes_branch already carries the $release_version release notes."
    else
      if git show "origin/main:$notes_index" | grep -Eq "^## $release_version "; then
        echo "Release notes for $release_version are already on main, but no open or merged $notes_branch pull request was found." >&2
        exit 1
      fi
      git switch -C "$notes_branch" origin/main
      updated_index="$work_dir/release-notes-index.md"
      if ! awk -v version="$release_version" -v notes="$release_section" '
        function lower(a, b,    x, y, i) {
          split(a, x, "."); split(b, y, ".")
          for (i = 1; i <= 3; i++) if (x[i] != y[i]) return x[i] + 0 < y[i] + 0
          return 0
        }
        !inserted && /^## [0-9]+\.[0-9]+\.[0-9]+ / && lower($2, version) {
          while ((getline entry < notes) > 0) print entry
          close(notes)
          inserted = 1
        }
        { print }
        END { exit !inserted }
      ' "$notes_index" >"$updated_index"; then
        echo "main has no release-notes section older than $release_version; cannot place its notes." >&2
        exit 1
      fi
      mv "$updated_index" "$notes_index"
      git add -- "$notes_index"
      git commit -m "Add $release_version release notes"
      git push --set-upstream origin "$notes_branch"
    fi

    body_file="$work_dir/main-pull-request-body.md"
    {
      printf 'EDOT Android %s is published: [%s](%s), [GitHub Release](%s).\n\n' "$release_version" "$tag" "$tag_url" "$release_url"
      printf 'Merging this pull request publishes the release notes on the documentation site.\n'
    } >"$body_file"
    main_pr=$(
      gh pr create \
        --repo "$repository" \
        --base main \
        --head "$notes_branch" \
        --title "Release notes for $release_version" \
        --body-file "$body_file"
    )
  fi

  # Validate both ephemeral remote tips before deleting either branch. Each
  # deletion is leased on the validated tip, so a branch that moves in
  # between is left alone rather than deleted.
  releasing_tip=$(git ls-remote --heads origin "refs/heads/$base_ref" | awk '{print $1}')
  if [[ -n $releasing_tip && $releasing_tip != "$release_sha" ]]; then
    echo "$base_ref moved: its tip is $releasing_tip, expected $release_sha. No patch branch was deleted." >&2
    exit 1
  fi
  patch_branch="patching/$release_version"
  patching_tip=$(git ls-remote --heads origin "refs/heads/$patch_branch" | awk '{print $1}')
  if [[ -n $patching_tip ]]; then
    git fetch --quiet origin "$patch_branch"
    if ! git merge-base --is-ancestor "$patching_tip" "$release_sha"; then
      echo "$patch_branch tip $patching_tip is not an ancestor of $release_sha. No patch branch was deleted." >&2
      exit 1
    fi
  fi
  [[ -z $releasing_tip ]] || git push --force-with-lease="refs/heads/$base_ref:$releasing_tip" origin --delete "$base_ref"
  [[ -z $patching_tip ]] || git push --force-with-lease="refs/heads/$patch_branch:$patching_tip" origin --delete "$patch_branch"
fi

if [[ -n ${GITHUB_OUTPUT:-} ]]; then
  {
    echo "release_url=$release_url"
    echo "main_pull_request_url=$main_pr"
  } >>"$GITHUB_OUTPUT"
fi
