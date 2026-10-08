#!/usr/bin/env bash
#
# Draft EDOT Android release-note JSON by dispatching the Draft release notes
# workflow on a release ref and printing the JSON it attaches to its run.
#
# Usage: draft-notes.sh <ref>
#
# Arguments:
#   ref  branch the release is prepared from: main or patching/x.y.z
#
# The workflow drafts from the same range Prepare release uses for that
# branch. This script runs it through dispatch.sh, downloads the run's
# `release-notes` artifact into a temporary directory outside the
# repository, prints the JSON on standard output, and removes the directory.
# Progress goes to standard error. It writes nothing to the repository.
#
# Exit status: 0 when the JSON is printed; 1 otherwise, with the run URL on
# standard error when there is a run; 2 on a usage error. When the run's
# artifact list has no `release-notes` artifact, standard error says
# "The run has no release-notes artifact": the branch runs workflows that
# predate it. Any other failure says what failed.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"

if [[ $# -ne 1 || -z $1 ]]; then
  echo "Usage: draft-notes.sh <ref>" >&2
  exit 2
fi
ref=$1

run=$("$script_dir/dispatch.sh" draft-release-notes.yml "$ref") || exit 1
run_id=${run%% *}
run_url=${run#* }

notes_dir=$(mktemp -d)
trap 'rm -rf "$notes_dir"' EXIT

# A branch whose workflows predate the artifact finishes without one. Read
# that from the run's artifact list, so a failed download is not mistaken
# for it.
if ! artifacts=$(
  gh api "repos/$repository/actions/runs/$run_id/artifacts" \
    --jq '[.artifacts[] | select(.name == "release-notes")] | length'
); then
  echo "Could not list the artifacts of the run: $run_url" >&2
  exit 1
fi
if [[ $artifacts -eq 0 ]]; then
  echo "The run has no release-notes artifact: $run_url" >&2
  exit 1
fi
if ! gh run download "$run_id" --repo "$repository" --name release-notes \
  --dir "$notes_dir" >&2; then
  echo "Could not download the release-notes artifact of the run: $run_url" >&2
  exit 1
fi
jq . "$notes_dir/release-notes.json"
