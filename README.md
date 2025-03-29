# apm-agent-android

Elastic OTel Android Agent

See the [documentation](https://www.elastic.co/guide/en/apm/agent/android/current/index.html) to find out more about its features, how to setup, and configuration details.

## Local testing

In order to use a local version of this agent, you'll need to publish it into your machine's
maven local repo. To do so, open up a terminal in this project's root dir and
run: `./gradlew publishToMavenLocal`. After that, you can apply the agent into an Android
application project (that uses [mavenLocal()](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api.artifacts.dsl/-repository-handler/maven-local.html) as one of its dependency repositories) 
by following the [Getting started](https://www.elastic.co/guide/en/apm/agent/android/current/setup.html)" guide and using the version defined [here](gradle.properties).