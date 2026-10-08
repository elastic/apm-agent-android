#!/usr/bin/env bash
#
# Preview an EDOT Android patch release: the source tag of a release line,
# the patch version Start patch would produce, and the cherry-pick
# candidates.
#
# Usage: patch-preview.sh <X.Y|latest> [pull-request-number ...]
#
# Arguments:
#   X.Y|latest           release line to patch; `latest` is the line of the
#                        highest release tag
#   pull-request-number  main pull requests the operator names, added to the
#                        candidates
#
# The source tag and the patch version come from the version.sh script of
# the shared release actions, fetched from elastic/oblt-actions at v1, so
# they follow the same rules as Start patch. Candidates are the main pull
# requests labeled `bug` merged after the source tag's date, plus the named
# ones. A candidate that changes .github/workflows/* cannot go through Start
# patch and is marked. This script does not check whether the tag can be
# patched or whether the patch branch exists: Start patch does. It fetches
# tags and writes nothing to the repository.
#
# Prints one JSON object:
#   line           the X.Y line
#   sourceTag      the line's highest release tag
#   sourceTagDate  that tag's date, UTC
#   patchVersion   the version Start patch would produce
#   candidates     number, title, url, mergedAt, labeledBug, and
#                  changesWorkflows for each candidate, in merge order
#
# Exit status: 0 on success; 1 when the line has no release, a value is
# invalid, or a read fails, with the reason on standard error; 2 on a usage
# error.

set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source-path=SCRIPTDIR source=repository.sh
source "$script_dir/repository.sh"
version_sh_path=edot-release/version/version.sh

if [[ $# -lt 1 || -z $1 ]]; then
  echo "Usage: patch-preview.sh <X.Y|latest> [pull-request-number ...]" >&2
  exit 2
fi
requested_line=$1
shift
for number in "$@"; do
  if [[ ! $number =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid pull request number '$number'." >&2
    exit 2
  fi
done

work_dir=$(mktemp -d)
trap 'rm -rf "$work_dir"' EXIT

git fetch --quiet --tags origin

# Run the shared version arithmetic rather than a copy of it.
version_sh="$work_dir/version.sh"
gh api -H 'Accept: application/vnd.github.raw' \
  "repos/elastic/oblt-actions/contents/$version_sh_path?ref=v1" >"$version_sh"
version() {
  TAG_PREFIX=v "$BASH" "$version_sh" "$@"
}

if [[ $requested_line == latest ]]; then
  source_tag=$(version highest-tag)
else
  source_tag=$(version highest-tag "$requested_line")
fi
patch_version=$(version next-patch "$source_tag")
line=${patch_version%.*}
source_tag_date=$(
  TZ=UTC git for-each-ref \
    --format='%(creatordate:format-local:%Y-%m-%dT%H:%M:%SZ)' \
    "refs/tags/$source_tag"
)

fields=number,title,url,mergedAt,labels,files
candidate='{
  number,
  title,
  url,
  mergedAt,
  labeledBug: any(.labels[]; .name == "bug"),
  changesWorkflows: any(.files[]; .path | startswith(".github/workflows/"))
}'

candidates=$(
  gh pr list --repo "$repository" --state merged --base main --label bug \
    --search "merged:>$source_tag_date" --limit 200 \
    --json "$fields" --jq "[.[] | $candidate]"
)
for number in "$@"; do
  named=$(gh pr view "$number" --repo "$repository" --json "$fields" --jq "$candidate")
  candidates=$(jq --argjson named "$named" '. + [$named]' <<<"$candidates")
done

jq -n \
  --arg line "$line" \
  --arg source_tag "$source_tag" \
  --arg source_tag_date "$source_tag_date" \
  --arg patch_version "$patch_version" \
  --argjson candidates "$candidates" '
  {
    line: $line,
    sourceTag: $source_tag,
    sourceTagDate: $source_tag_date,
    patchVersion: $patch_version,
    candidates: ($candidates | unique_by(.number) | sort_by(.mergedAt // ""))
  }'
