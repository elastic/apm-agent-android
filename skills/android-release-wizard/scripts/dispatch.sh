#!/usr/bin/env bash
#
# Dispatch one of the repository's release workflows, wait for the run it
# starts, and report the run.
#
# Usage: dispatch.sh <workflow> <ref> [<input>=<value> ...]
#
# Arguments:
#   workflow     workflow file name, such as prepare-release.yml
#   ref          branch to run the workflow on, such as main
#   input=value  a workflow input, such as `line=1.10`; `<input>=@-` reads
#                the value from standard input, such as
#                `release_notes=@-`
#
# The release wizard dispatches every workflow through this script, so the
# dispatch exists once. The dispatch API returns the ID of the run it
# creates, so the script waits for exactly that run through watch-run.sh.
# Progress goes to standard error.
#
# On success, prints `<run-id> <run-url>` on standard output.
#
# Exit status: 0 when the run succeeds; 1 when the dispatch fails or returns
# no run, or with watch-run.sh's message when the run fails or its outcome
# is unknown; 2 on a usage error.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"

usage="Usage: dispatch.sh <workflow> <ref> [<input>=<value> ...]"
if [[ $# -lt 2 || -z $1 || -z $2 ]]; then
  echo "$usage" >&2
  exit 2
fi
workflow=$1
ref=$2
shift 2

fields=(-f "ref=$ref" -F return_run_details=true)
for input in "$@"; do
  name=${input%%=*}
  value=${input#*=}
  if [[ $input != *=* || -z $name ]]; then
    echo "$usage" >&2
    exit 2
  fi
  # -F reads `@-` from standard input; -f keeps any other value literal.
  if [[ $value == @- ]]; then
    fields+=(-F "inputs[$name]=@-")
  else
    fields+=(-f "inputs[$name]=$value")
  fi
done

# return_run_details makes the API answer with the run it created.
if ! run_id=$(
  gh api --method POST "repos/$repository/actions/workflows/$workflow/dispatches" \
    "${fields[@]}" --jq '.workflow_run_id // ""'
); then
  echo "The $workflow dispatch on $ref failed." >&2
  exit 1
fi
if [[ -z $run_id ]]; then
  echo "The $workflow dispatch on $ref returned no run details." >&2
  exit 1
fi

exec "$script_dir/watch-run.sh" "$run_id"
