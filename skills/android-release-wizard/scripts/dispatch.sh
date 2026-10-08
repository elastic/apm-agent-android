#!/usr/bin/env bash
#
# Dispatch one of the repository's release workflows, wait for the run it
# starts, and report the run.
#
# Usage: dispatch.sh <workflow> <ref> [gh workflow run input flags ...]
#
# Arguments:
#   workflow     workflow file name, such as prepare-release.yml
#   ref          branch to run the workflow on, such as main
#   input flags  passed unchanged to `gh workflow run`, such as
#                `-f line=1.10` or `-F release_notes=@-`; standard input is
#                passed through, so `@-` reads the caller's standard input
#
# The release wizard dispatches every workflow through this script, so the
# dispatch, the lookup of the new run, and the wait exist once. Only a run
# of that workflow on that ref created at or after the dispatch counts.
# Progress goes to standard error.
#
# On success, prints `<run-id> <run-url>` on standard output.
#
# Exit status: 0 when the run succeeds; 1 when the dispatch fails, no run
# appears, or the run fails, with the failed steps' log and the run URL on
# standard error; 2 on a usage error.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"

if [[ $# -lt 2 || -z $1 || -z $2 ]]; then
  echo "Usage: dispatch.sh <workflow> <ref> [gh workflow run input flags ...]" >&2
  exit 2
fi
workflow=$1
ref=$2
shift 2

# Only a run created at or after this moment belongs to this dispatch. Run
# timestamps have whole seconds, like this one, so a run started in the same
# second still matches.
since=$(date -u +%Y-%m-%dT%H:%M:%SZ)
gh workflow run "$workflow" --repo "$repository" --ref "$ref" "$@" >&2

# The run takes a few seconds to appear after the dispatch. The earliest
# matching run is this dispatch's; a later one belongs to another dispatch.
run_id=
for _ in $(seq 1 30); do
  run_id=$(
    gh run list --repo "$repository" --workflow "$workflow" \
      --event workflow_dispatch --branch "$ref" --limit 5 \
      --json databaseId,createdAt \
      --jq "[.[] | select(.createdAt >= \"$since\")] | sort_by(.createdAt, .databaseId) | first | .databaseId // empty"
  )
  [[ -n $run_id ]] && break
  sleep 2
done
if [[ -z $run_id ]]; then
  echo "No $workflow run on $ref appeared after the dispatch at $since." >&2
  exit 1
fi
run_url="https://github.com/$repository/actions/runs/$run_id"
echo "Waiting for $run_url" >&2

if ! gh run watch "$run_id" --repo "$repository" --exit-status >&2; then
  gh run view "$run_id" --repo "$repository" --log-failed >&2 || true
  echo "The $workflow run failed: $run_url" >&2
  exit 1
fi
echo "$run_id $run_url"
