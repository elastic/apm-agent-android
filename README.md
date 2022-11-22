# apm-agent-android

# Work in progress ⚠️🚧

Elastic APM Android Agent

See the [documentation](docs) for setup and configuration details.

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