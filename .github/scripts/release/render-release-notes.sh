#!/usr/bin/env bash
#
# Render the release-note JSON as the Markdown section for
# docs/release-notes/index.md.
#
# Usage: render-release-notes.sh <source-json> <version>
#
# Validates the JSON shape, refuses input that still has `uncategorized`
# items or has no items, and prints a `## X.Y.Z` section with the anchors the
# docs site expects: an untitled list for dependencies, then "Features and
# enhancements" and "Fixes" subsections. Empty groups are omitted and
# `[Breaking]` items sort first. The same section becomes the GitHub Release
# body.

set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: render-release-notes.sh <source-json> <version>" >&2
  exit 2
fi

source_file=$1
version=$2
release_date=$(date +'%B %d, %Y' | sed 's/ 0/ /')
repository=${GITHUB_REPOSITORY:-elastic/apm-agent-android}
server_url=${GITHUB_SERVER_URL:-https://github.com}

if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Invalid release version: $version" >&2
  exit 1
fi

jq -e '
  type == "object"
  and (.dependencies | type == "array")
  and (.featuresEnhancements | type == "array")
  and (.fixes | type == "array")
  and (.uncategorized | type == "array")
  and ([.dependencies[], .featuresEnhancements[], .fixes[], .uncategorized[]]
    | all(
        type == "object"
        and (.message | type == "string" and length > 0 and (test("[\\r\\n]") | not))
        and (
          .prId == null
          or (.prId | type == "number" and . == floor and . > 0)
          or (.prId | type == "string" and test("^[1-9][0-9]*$"))
        )
      )
  )
' "$source_file" >/dev/null || {
  echo "Release notes must contain valid dependencies, featuresEnhancements, fixes, and uncategorized arrays." >&2
  exit 1
}

if [[ $(jq '.uncategorized | length' "$source_file") -ne 0 ]]; then
  echo "Release notes contain uncategorized items; place every item before preparing the release." >&2
  exit 1
fi
if [[ $(jq '[.dependencies[], .featuresEnhancements[], .fixes[]] | length' "$source_file") -eq 0 ]]; then
  echo "Release notes must contain at least one item." >&2
  exit 1
fi

# Existing notes use `<base>-release-notes` for the version heading and
# `<base>-features-enhancements` / `<base>-fixes` for the subsections.
anchor_base="elastic-apm-android-agent-${version//./}"
anchor="$anchor_base-release-notes"

render_items() {
  local filter=$1
  jq -r \
    --arg server "$server_url" \
    --arg repository "$repository" \
    "$filter
     | sort_by(if (.message | startswith(\"[Breaking]\")) then 0 else 1 end)
     | .[]
     | \"* \" + .message
       + (if .prId == null then \"\"
          else \": [#\" + (.prId | tostring) + \"](\" + \$server + \"/\" + \$repository + \"/pull/\" + (.prId | tostring) + \")\"
          end)" \
    "$source_file"
}

printf '## %s [%s]\n' "$version" "$anchor"
printf '**Release date:** %s\n' "$release_date"

if [[ $(jq '.dependencies | length' "$source_file") -gt 0 ]]; then
  printf '\n'
  render_items '.dependencies'
fi
if [[ $(jq '.featuresEnhancements | length' "$source_file") -gt 0 ]]; then
  printf '\n### Features and enhancements [%s-features-enhancements]\n\n' "$anchor_base"
  render_items '.featuresEnhancements'
fi
if [[ $(jq '.fixes | length' "$source_file") -gt 0 ]]; then
  printf '\n### Fixes [%s-fixes]\n\n' "$anchor_base"
  render_items '.fixes'
fi
printf '\n'
