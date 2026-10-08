#!/usr/bin/env bash
#
# Report where EDOT Android's release process stands and what the operator
# does next, read from GitHub state only.
#
# Usage: release-state.sh
#
# The release wizard runs this before anything else, so it reads the phase
# from the release branches and their pull requests instead of from a
# record. It only reads: it creates, merges, and deletes nothing.
#
# Prints one JSON object:
#   phase                 `in-flight` while a releasing/* branch exists (the
#                         next preparation stops on it), `patching` while
#                         only a patching/* branch exists, else `idle`
#   releasingBranches     releasing/x.y.z branch names
#   patchingBranches      patching/x.y.z branch names
#   patchNotesBranches    patch-notes/x.y.z branch names
#   openPullRequests      open pull requests from or into those branches:
#                         number, url, head, base
#   nextActions           the operator's next actions, one sentence each
#
# Exit status: 0 on success, 1 when a GitHub read fails, 2 on a usage error.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"

if [[ $# -ne 0 ]]; then
  echo "Usage: release-state.sh" >&2
  exit 2
fi

# Branch names under one prefix, as a JSON array.
branches() {
  gh api "repos/$repository/git/matching-refs/heads/$1/" \
    --jq '[.[].ref | ltrimstr("refs/heads/")]'
}

# Merged pull requests from a branch into main, as a JSON array. Android
# does not delete a merged head, so a merged pull request whose head still
# exists means the operator has not deleted that branch yet.
merged_into_main() {
  gh pr list --repo "$repository" --state merged --base main --head "$1" \
    --json number,url --jq '[.[] | {number, url}]'
}

releasing=$(branches releasing)
patching=$(branches patching)
patch_notes=$(branches patch-notes)

open_pull_requests=$(
  gh pr list --repo "$repository" --state open --limit 200 \
    --json number,url,headRefName,baseRefName \
    --jq '[.[]
      | select((.headRefName | test("^(releasing|prepare|patch-notes)/"))
          or (.baseRefName | test("^(releasing|patching)/")))
      | {number, url, head: .headRefName, base: .baseRefName}]'
)

merged='{}'
for branch in $(jq -r '.[]' <<<"$releasing") $(jq -r '.[]' <<<"$patch_notes"); do
  merged=$(jq --arg branch "$branch" --argjson prs "$(merged_into_main "$branch")" \
    '.[$branch] = $prs' <<<"$merged")
done

jq -n \
  --argjson releasing "$releasing" \
  --argjson patching "$patching" \
  --argjson patch_notes "$patch_notes" \
  --argjson open "$open_pull_requests" \
  --argjson merged "$merged" '
  def version: sub("^[^/]+/"; "");
  def open_pr($head; $base): [$open[] | select(.head == $head and .base == $base)] | first;
  def releasing_action:
    . as $branch
    | ($branch | version) as $v
    | open_pr("prepare/\($v)"; $branch) as $prepare
    | open_pr($branch; "main") as $into_main
    | if $prepare then
        "Review and merge the preparation pull request \($prepare.url) into \($branch) when its checks are green; the merge publishes \($v)."
      elif $into_main then
        "Review and merge the pull request \($into_main.url) from \($branch) into main, then delete \($branch) by hand."
      elif ($merged[$branch] | length) > 0 then
        "Delete \($branch) by hand: its pull request \($merged[$branch][0].url) into main is merged, and the next preparation stops while the branch exists."
      else
        "\($branch) exists without an open preparation pull request or pull request into main. Check the latest Publish release run for \($v) and follow Failure and recovery in RELEASING.md."
      end;
  def patch_notes_action:
    . as $branch
    | ($branch | version) as $v
    | open_pr($branch; "main") as $notes
    | if $notes then
        "Patch \($v) is published. Review and merge the notes pull request \($notes.url) into main, then delete \($branch) by hand."
      elif ($merged[$branch] | length) > 0 then
        "Delete \($branch) by hand: its notes pull request \($merged[$branch][0].url) into main is merged."
      else
        "\($branch) exists without an open notes pull request into main. Check the latest Publish release run for \($v)."
      end;
  def patching_action:
    . as $branch
    | ($branch | version) as $v
    | "Patch \($v) is open on \($branch). Add fixes through pull requests into it, then draft notes and prepare the release from it.";
  {
    phase: (if ($releasing | length) > 0 then "in-flight"
            elif ($patching | length) > 0 then "patching"
            else "idle" end),
    releasingBranches: $releasing,
    patchingBranches: $patching,
    patchNotesBranches: $patch_notes,
    openPullRequests: $open,
    nextActions: (
      [$releasing[] | releasing_action]
      + [$patch_notes[] | patch_notes_action]
      + (if ($releasing | length) == 0 then [$patching[] | patching_action] else [] end)
    )
  }'
