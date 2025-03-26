#!/bin/bash

bump_minor_version() {
    local version="$1"

    if [[ $version =~ ^([0-9]+)\.([0-9]+)[0-9\.]*$ ]]; then
        local major_version=${BASH_REMATCH[1]}
        local minor_version=${BASH_REMATCH[2]}

        local new_minor_version=$((minor_version + 1))
        local new_version="$major_version.$new_minor_version.0"

        echo "$new_version"
    else
        echo "Could not find minor version in: $version" >&2
        return 1
    fi
}

bump_minor_version "$1"
