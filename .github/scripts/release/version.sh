#!/usr/bin/env bash
#
# Version arithmetic for the release scripts. Nobody types a version; every
# version is derived here from the highest `vX.Y.Z` tag.
#
#   highest-tag                        the latest release tag by version order
#   release-version <tag> <dev> <bump> the version to release; fails when
#                                      <dev> is not the next minor `-SNAPSHOT`
#                                      after <tag>, which catches an unmerged
#                                      release PR
#   next-development <version>         the next minor `-SNAPSHOT` after a
#                                      release version or tag

set -euo pipefail

usage() {
  cat >&2 <<'EOF'
Usage:
  version.sh highest-tag
  version.sh release-version <previous-tag> <development-version> <minor|major>
  version.sh next-development <release-version-or-tag>
EOF
  exit 2
}

parse_version() {
  local value=${1#v}
  if [[ ! $value =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    echo "Invalid semantic version: $1" >&2
    exit 1
  fi
  VERSION_MAJOR=${BASH_REMATCH[1]}
  VERSION_MINOR=${BASH_REMATCH[2]}
  VERSION_PATCH=${BASH_REMATCH[3]}
}

next_development() {
  parse_version "$1"
  printf '%s.%s.0-SNAPSHOT\n' "$VERSION_MAJOR" "$((VERSION_MINOR + 1))"
}

case ${1:-} in
  highest-tag)
    [[ $# -eq 1 ]] || usage
    # Only exact vX.Y.Z tags count; a glob alone would also match names such
    # as v2.0.0-rc1 and make preparation fail on them.
    tag=$(
      git tag --list 'v*' --sort=-version:refname \
        | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' \
        | head -n 1
    )
    if [[ -z $tag ]]; then
      echo "No release tag matching vX.Y.Z was found." >&2
      exit 1
    fi
    parse_version "$tag"
    printf '%s\n' "$tag"
    ;;
  release-version)
    [[ $# -eq 4 ]] || usage
    previous_tag=$2
    development_version=$3
    bump=$4
    expected=$(next_development "$previous_tag")
    if [[ $development_version != "$expected" ]]; then
      echo "Expected development version $expected after $previous_tag, found $development_version." >&2
      exit 1
    fi
    parse_version "$previous_tag"
    case $bump in
      minor)
        printf '%s.%s.0\n' "$VERSION_MAJOR" "$((VERSION_MINOR + 1))"
        ;;
      major)
        printf '%s.0.0\n' "$((VERSION_MAJOR + 1))"
        ;;
      *)
        echo "Unsupported bump '$bump'; expected minor or major." >&2
        exit 1
        ;;
    esac
    ;;
  next-development)
    [[ $# -eq 2 ]] || usage
    next_development "$2"
    ;;
  *)
    usage
    ;;
esac
