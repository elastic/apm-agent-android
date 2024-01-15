# Buildkite

This README provides an overview of the Buildkite pipeline to automate the build and publishing process.

## Release pipeline

The Buildkite pipeline for the APM Agent Android is responsible for the releases.

### Pipeline Configuration

To view the pipeline and its configuration, click [here](https://buildkite.com/elastic/apm-agent-android-release) or
go to the definition in the `elastic/ci` repository.

### Credentials

The release team provides the credentials to publish the artifacts in Maven Central/Gradle Plugin and sign them
with the GPG.

If further details are needed, please go to [pre-command](hooks/pre-command).
