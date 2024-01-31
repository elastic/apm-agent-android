# apm-agent-android

> [!NOTE]
> This agent is based on
> the [OpenTelemetry Android lib](https://github.com/open-telemetry/opentelemetry-android) which is
> currently unstable, however, we're proactively contributing to it in order to make it stable as
> soon as possible (on its version 1.x) and will avoid introducing API breaking changes in the
> Elastic agent in the meantime.

Elastic APM Android Agent

See the [documentation](https://www.elastic.co/guide/en/apm/agent/android/current/index.html) for
setup and configuration details.

## Documentation

To build the documentation for this project you must first clone
the [`elastic/docs` repository](https://github.com/elastic/docs/). Then run the following commands:

```bash
# Set the location of your repositories
export GIT_HOME="/<fullPathTYourRepos>"

# Build the Android Agent documentation
$GIT_HOME/docs/build_docs --doc $GIT_HOME/apm-agent-android/docs/index.asciidoc --chunk 1 --open
```

## Local testing

In order to use a local version of this agent you'll need to publish it locally into your machine's
maven local repo. In order to do that, simply open up a terminal in this project's root dir and
run: `./gradlew publishToMavenLocal`. After that, you can apply this agent into an Android
application project by following the "Set up" process defined [here](docs).