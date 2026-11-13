#!/usr/bin/env bash
# Copyright Elastic Technologies Inc. and/or licensed to
# Elastic Technologies Inc. under one or more contributor license
# agreements. See the NOTICE file distributed with this work for
# additional information regarding copyright ownership.
# Elastic Technologies Inc. licenses this file to you under the Apache
# License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Wait for a workflow run in the repository to finish and report its
# outcome.
#
# Usage: watch-run.sh <run-id>
#
# Arguments:
#   run-id  ID of the workflow run, such as the one dispatch.sh reports
#
# dispatch.sh waits for every run it starts through this script. Run it
# directly to follow a run whose outcome dispatch.sh could not establish.
# The run's conclusion decides the outcome, not the exit status of
# `gh run watch`. Progress goes to standard error.
#
# On success, prints `<run-id> <run-url>` on standard output.
#
# Exit status: 0 when the run succeeds; 1 when the run fails, with the
# failed steps' log and the run URL on standard error, or when the run
# cannot be followed to its end, with the run URL and a request to run this
# script again instead of dispatching again; 2 on a usage error.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"

if [[ $# -ne 1 || ! $1 =~ ^[1-9][0-9]*$ ]]; then
  echo "Usage: watch-run.sh <run-id>" >&2
  exit 2
fi
run_id=$1
run_url="https://github.com/$repository/actions/runs/$run_id"
unknown="Its outcome is unknown; run watch-run.sh $run_id again instead of dispatching again: $run_url"
echo "Waiting for $run_url" >&2

# A run can be unreadable for a few seconds after its dispatch.
for attempt in 1 2 3 4 5 6; do
  if gh run view "$run_id" --repo "$repository" --json status >/dev/null 2>&1; then
    break
  fi
  if [[ $attempt -eq 6 ]]; then
    echo "Run $run_id cannot be read yet. $unknown" >&2
    exit 1
  fi
  sleep 5
done

# The watch also exits non-zero when it loses track of a run that is still
# in progress.
gh run watch "$run_id" --repo "$repository" --compact --interval 10 >&2 || true
conclusion=$(
  gh run view "$run_id" --repo "$repository" --json status,conclusion \
    --jq 'if .status == "completed" then .conclusion else "" end'
) || conclusion=
if [[ -z $conclusion ]]; then
  echo "Stopped watching run $run_id before it finished. $unknown" >&2
  exit 1
fi
if [[ $conclusion != success ]]; then
  gh run view "$run_id" --repo "$repository" --log-failed >&2 || true
  echo "Run $run_id ended with $conclusion: $run_url" >&2
  exit 1
fi
echo "$run_id $run_url"
