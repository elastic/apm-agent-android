#!/usr/bin/env bash
## This script uploads the given logs to a google bucket
##

# Let's avoid failing the build
set +e

ls -l $1 || true
echo 'TBC upload artifacts to google'
