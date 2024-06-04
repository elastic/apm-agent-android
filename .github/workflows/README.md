# CI/CD

There are 2 main stages that run on GitHub actions for every push or Pull Request:

* Linting
* Test

## Scenarios

* Tests should be triggered on branch, tag and PR basis.
* Commits that are only affecting the docs files should not trigger any test or similar stages that are not required.
* **This is not the case yet**, but if Github secrets are required then Pull Requests from forked repositories won't run any build accessing those secrets. If needed, then create a feature branch.

## How to interact with the CI?

### On a PR basis

Once a PR has been opened then there are two different ways you can trigger builds in the CI:

1. Commit based.
1. UI based, any Elasticians can force a build through the GitHub UI

### Branches

Every time there is a merge to main or any release branches the main workflow will lint and test all on Linux.

## Release

The release automation relies on Buildkite for generating and publishing the artifacts,
for further details please go to [the buildkite folder](../../.buildkite/README.md).

## OpenTelemetry

Every workflow and its logs are exported to OpenTelemetry traces/logs/metrics. Those details can be seen in
[here](https://ela.st/oblt-ci-cd-stats) (**NOTE**: only available for Elasticians).
