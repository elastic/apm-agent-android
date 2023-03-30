#!/usr/bin/env bash
set -euo pipefail

.ci/release.sh | tee release.out
