#!/usr/bin/env bash
#
# List the pull requests that go into the next release.
#
# Usage: pr-range.sh [ref] [previous-tag]
#
# Arguments:
#   ref           commit or ref at the end of the range (default HEAD)
#   previous-tag  release tag at the start of the range (default highest
#                 vX.Y.Z tag)
#
# Environment:
#   GITHUB_REPOSITORY  owner/repository to query (default current gh repo)
#   GH_TOKEN           GitHub CLI authentication in CI; local gh
#                      authentication is used when unset
#
# Walks the first-parent commits between the previous release tag and `ref`
# (default HEAD) and resolves each one to its merged pull request through the
# GitHub API. Because `main` is squash-merged, each commit is one PR. A commit
# with no PR resolves through a full-SHA cherry-pick trailer when present,
# then stops if no PR can be found. PRs from `releasing/*`, `prepare/*`, or
# `patch-notes/*` branches are release bookkeeping and are left out. Prints
# JSON with the tag, the range, and one entry per PR (number, title, URL,
# labels).

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ref=${1:-HEAD}
previous_tag=${2:-}
repository=${GITHUB_REPOSITORY:-}

if [[ -z $repository ]]; then
  repository=$(gh repo view --json nameWithOwner --jq .nameWithOwner)
fi
if [[ -z $previous_tag ]]; then
  previous_tag=$("$script_dir/version.sh" highest-tag)
fi

head_sha=$(git rev-parse "$ref")
base_sha=$(git merge-base "$previous_tag" "$head_sha")
pull_requests='[]'

resolve_pull_request() {
  local commit=$1
  local associated
  associated=$(
    gh api \
      -H "Accept: application/vnd.github+json" \
      "repos/$repository/commits/$commit/pulls?per_page=100"
  )
  jq -c \
    --arg repository "$repository" \
    --arg commit "$commit" \
    '[
       .[]
       | select(
           .merged_at != null
           and .base.repo.full_name == $repository
           and .merge_commit_sha == $commit
         )
     ] as $exact
     | if ($exact | length) == 1 then
         $exact[0]
       else
         [.[] | select(.merged_at != null and .base.repo.full_name == $repository)] as $eligible
         | if ($eligible | length) == 1 then $eligible[0] else empty end
       end' <<<"$associated"
}

while IFS= read -r commit_sha; do
  [[ -n $commit_sha ]] || continue
  # Prefer the PR whose merge commit is this exact commit. Fall back to the
  # single merged PR in this repository when the API lists only one. A bot
  # cherry-pick has no PR of its own and resolves through its `-x` trailer.
  pull_request=$(resolve_pull_request "$commit_sha")
  if [[ -z $pull_request ]]; then
    commit_message=$(git show -s --format=%B "$commit_sha")
    if [[ $commit_message =~ \(cherry\ picked\ from\ commit\ ([0-9a-fA-F]{40})\) ]]; then
      pull_request=$(resolve_pull_request "${BASH_REMATCH[1]}")
    fi
  fi
  if [[ -z $pull_request ]]; then
    echo "Commit $commit_sha has no associated merged pull request in $repository." >&2
    exit 1
  fi

  head_ref=$(jq -r '.head.ref' <<<"$pull_request")
  if [[ $head_ref == releasing/* || $head_ref == prepare/* || $head_ref == patch-notes/* ]]; then
    continue
  fi

  item=$(
    jq -c \
      '{
        number: .number,
        title: .title,
        url: .html_url,
        labels: [.labels[].name]
      }' <<<"$pull_request"
  )
  pull_requests=$(jq -c --argjson item "$item" '. + [$item]' <<<"$pull_requests")
done < <(git rev-list --first-parent --reverse "$base_sha..$head_sha")

jq -n \
  --arg previous_tag "$previous_tag" \
  --arg base_sha "$base_sha" \
  --arg head_sha "$head_sha" \
  --argjson pull_requests "$pull_requests" \
  '{
    previousTag: $previous_tag,
    baseSha: $base_sha,
    headSha: $head_sha,
    pullRequests: $pull_requests
  }'
