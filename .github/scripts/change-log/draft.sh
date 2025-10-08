#!/bin/bash -e

version=$(cat gradle.properties | grep version= | sed s/version=//g)

if [[ $version =~ ([0-9]+)\.([0-9]+)\.0 ]]; then
  echo "Version found: ${version}"
  major="${BASH_REMATCH[1]}"
  minor="${BASH_REMATCH[2]}"
else
  echo "unexpected version: $version"
  exit 1
fi

if [[ $minor == 0 ]]; then
  prior_major=$((major - 1))
  prior_minor=$(sed -n "s/^## Version $prior_major\.\([0-9]\+\)\..*/\1/p" CHANGELOG.md | head -1)
  if [[ -z $prior_minor ]]; then
    # assuming this is the first release
    range=
  else
    range="v$prior_major.$prior_minor.0..HEAD"
  fi
else
  range="v$major.$((minor - 1)).0..HEAD"
fi

echo "[$(
git log --reverse \
        --perl-regexp \
        --author='^((?!elastic-renovate).*)$' \
        --pretty=format:"%s" \
        "$range" \
  | sed -r 's;^(.+) \(#([0-9]+)\)$;{"message":"\1", "prId":"\2"},;g' \
  | sed '$ s/,$//g'
)]"