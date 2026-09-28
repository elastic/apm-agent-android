#!/usr/bin/env bash
#
# Start a patch release from the newest tag on a release line. The script
# validates every requested pull request, cuts `patching/X.Y.Z` from the tag,
# cherry-picks the fixes, and pushes once only after every local operation
# succeeds. The branch keeps the tag's release tooling: Prepare release and
# Publish release run from the branch, so the tag must already contain them.
#
# Environment:
#   LINE                 release line in X.Y form (required)
#   PULL_REQUESTS        optional comma- or space-separated PR numbers
#   GITHUB_REPOSITORY    owner/repository to query (required)
#   GH_TOKEN             GitHub CLI authentication (required in CI)
#   GITHUB_STEP_SUMMARY  GitHub Actions summary file (optional)

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
line=${LINE:?LINE is required}
pull_requests=${PULL_REQUESTS:-}
repository=${GITHUB_REPOSITORY:?GITHUB_REPOSITORY is required}
work_dir=build/release-automation

source_tag=$("$script_dir/version.sh" highest-tag "$line")
if ! git cat-file -e "$source_tag:.github/scripts/release/publish-guard.sh" 2>/dev/null; then
  echo "$source_tag predates the automated release process; patch this release by hand." >&2
  exit 1
fi

release_version=$("$script_dir/version.sh" next-patch "$source_tag")
patch_branch="patching/$release_version"
if git ls-remote --exit-code --heads origin "refs/heads/$patch_branch" >/dev/null 2>&1; then
  echo "$patch_branch already exists; cherry-pick further fixes by hand through pull requests into that branch." >&2
  exit 1
fi

mkdir -p "$work_dir"
requested_file="$work_dir/patch-pull-requests.tsv"
: >"$requested_file"
for token in ${pull_requests//,/ }; do
  number=${token#\#}
  if [[ ! $number =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid pull request '$token'; expected numbers separated by spaces or commas." >&2
    exit 1
  fi
  pr=$(
    gh pr view "$number" \
      --repo "$repository" \
      --json number,state,baseRefName,mergedAt,mergeCommit
  )
  if ! jq -e '.state == "MERGED" and .baseRefName == "main"' <<<"$pr" >/dev/null; then
    echo "Pull request #$number must be merged into main in $repository." >&2
    exit 1
  fi
  jq -r '[.mergedAt, (.number | tostring), .mergeCommit.oid] | @tsv' <<<"$pr" >>"$requested_file"
done
LC_ALL=C sort -o "$requested_file" "$requested_file"

git switch -c "$patch_branch" "$source_tag"

applied_prs=
while IFS=$'\t' read -r merged_at number merge_sha; do
  [[ -n ${number:-} ]] || continue
  # `-x` gives pr-range.sh an auditable way to resolve the original main PR.
  if ! git cherry-pick -x "$merge_sha"; then
    git cherry-pick --abort
    echo "Cherry-pick for pull request #$number conflicted. Dispatch again without #$number, then cherry-pick it by hand through a pull request into $patch_branch. Nothing was pushed." >&2
    exit 1
  fi
  applied_prs+="#$number "
done <"$requested_file"

# All checks and cherry-picks succeeded. This is the only push.
git push --set-upstream origin "$patch_branch"

if [[ -n ${GITHUB_STEP_SUMMARY:-} ]]; then
  {
    printf '### Started patch release %s\n\n' "$release_version"
    printf -- '- Branch: `%s`\n' "$patch_branch"
    printf -- '- Source tag: `%s`\n' "$source_tag"
    printf -- '- Version to be produced: `%s`\n' "$release_version"
    if [[ -n $applied_prs ]]; then
      printf -- '- Applied pull requests: %s\n' "${applied_prs% }"
    else
      printf -- '- Applied pull requests: none\n'
    fi
  } >>"$GITHUB_STEP_SUMMARY"
fi
