# Releasing

This document describes the steps required to publish a release to [Maven Central](https://central.sonatype.com/) and the
[Gradle Plugin Portal](https://plugins.gradle.org/), for all the regular libraries and Gradle plugin modules respectively, via the Github Actions configured
for this repository. For more technical details of the publishing process and configuration that happens under the hood, take a look at
the [build-tools's README file](build-tools/README.md).

## Release steps

### 1. Check version to release

Make sure that the [gradle.properties](gradle.properties) `version` property is set to the value you want to release. If not, create a PR to change it before continuing with
the release process.

### 2. Prepare a release branch

This is done by running [this action](https://github.com/elastic/apm-agent-android/actions/workflows/prepare-release.yml), which has 2 parameters:
  - The branch. This must be `main`.
  - The release notes. This must be provided as a single-line JSON with [this format](.github/scripts/generate-release-notes/sample.json).

[!TIP]
> You can use [this action](https://github.com/elastic/apm-agent-android/actions/workflows/draft-changelog.yml) to generate release notes based on the git diff since the last release.
> You should still check the result manually before entering it into the prepare release action, just in case it contains entries that don't make sense to display on the release notes page.
> The action will print the release notes to the console in JSON format.

This action should create a PR to a new release branch with the release notes and NOTICE file changes, if any. Review it and merge it.

### 3. Launch the release process

This is done by running [this action](https://github.com/elastic/apm-agent-android/actions/workflows/release.yml) where you'll need to provide the following parameters:
  - The branch. This must be the release branch created as part of the previous step. Its name must be `release/{version}`, where "{version}" is the one you're about to release.
  - The repo to deploy to. The options are:
    - `all` - Deploy all the artifacts, the ones that will go to maven central and also the Gradle plugin portal ones. This is the default option and should be left as is for a regular release.
    - `mavenCentral` - Only deploys the maven central artifacts. This is useful in case a previous release attempt only succeeded at releasing to the Gradle plugin portal but the maven central release failed.
    - `pluginPortal` - Only deploys the Gradle plugin artifacts. This is useful in case a previous release attempt only succeeded at releasing to maven central but the Gradle plugin portal release failed.
  - A dry-run checkbox. This is for IT use, leave it unchecked for a release.

The release action should do the following:
  - Release the artifacts to the specified repos.
  - Create a GitHub release and a tag with the newly released version.

[!NOTE]
> Maven central tends to have a delay of roughly half an hour, more or less, before making the newly published artifacts actually available
> for fetching them.

### 4. Prepare for the next release

If all went well on step 3, it should have automatically created a PR against `main` to update it and prepare it for a new release. Review and merge it.

## Patch release

A patch release requires to update the relevant release branch (previously created during [step #2](#2-prepare-a-release-branch))
to ensure that:

* The version is updated, that is, the patch number is increased. You must create a PR against the relevant release branch to update it.
* The patch/changes are merged into the relevant release branch.

After those items are in place, you can continue with the release process from [step #3](#3-launch-the-release-process).