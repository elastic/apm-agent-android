# Publishing tools for the APM Android project

This is a Gradle plugin that provides several tools needed at compile time on the APM Android
project. In order to apply this plugin, the following must be added into the root
project `build.gradle` file:

```groovy
plugins {
    id 'co.elastic.otel.publishing'
}
```

Table of Contents
=================

* [Creating NOTICE files](#creating-notice-files)
  * [Troubleshooting](#troubleshooting)
* [Adding source headers](#adding-source-headers)
* [Publishing](#publishing)
  * [Publishing parameters](#publishing-parameters)
  * [Publishing to Maven Central](#publishing-to-maven-central)
  * [Publishing to the Gradle Plugin Portal](#publishing-to-the-gradle-plugin-portal)

## Creating NOTICE files

Each subproject will get a new gradle task named `createNoticeFile` which takes care of gathering
all of its direct dependencies, then checking their POM files looking for their licenses, and then
it looks at their artifact files looking for NOTICE files to be merged (if any). Once all this
information is gathered, the subproject NOTICE file is generated and placed
in `subproject-dir/src/main/resources/META-INF/NOTICE`. Alongside the NOTICE files, there's also a
metadata file generated and placed in `subproject-dir/metadata/notice.properties`, which contains
information about the latest generated NOTICE file than can be verified later on.

The root project will also get its own `createNoticeFile` task, though it will be slightly different
than the one for its subprojects since it won't collect its own dependencies, but instead it will
reuse the dependencies' licenses and notice files gathered by its subprojects, then will merge
them (without any duplication of data) and create a "meta" NOTICE file containing all of the
subprojects dependencies info. The root NOTICE file will be generated at the root dir of this
project.

The following command will trigger the notice files generation process:

```text
./gradlew createNoticeFile
```

### Troubleshooting

Some POM files don't contain the dependency's license information, if that's the case,
the `createNoticeFile` task will raise an error during execution listing which dependencies don't
provide their license, when that happens, the license information must be provided manually in a
plain text file with a list of newline-separated dependencies, each with the following
format: `group:artifact-id:version|[LICENSE_ID]`. The format is composed of the dependency's GAV
coordinates (same as the ones used when adding them in the `build.gradle` files), then a
pipeline `|` followed by the licence's ID.

> The license ID must be available within this tool's `resources/licenses_ids.txt` file. More info
> on [How to add licenses IDs](docs/adding-license-ids.md).

Example of the contents of a file with two mappings:

```text
org.slf4j:slf4j-api:2.0.0|mit
com.squareup.okhttp3:okhttp:3.11.0|apache_v2
```

The "manually provided licenses" file must be set within the `build.gradle` file where the
dependencies being mapped to their licenses are defined. The way to reference the mappings file is
as shown below:

```groovy
// build.gradle.kts containing the dependencies that need manual mapping.
licensesConfig {
    manualMappingFile = "path/to/the/mappings/file.txt"
}
```

## Adding source headers

This work is triggered when building this project so there's nothing manual to be done about it. The
source headers are added using [spotless](https://github.com/diffplug/spotless) which is configured
for both `java` and `kotlin` source files
in [here](src/main/java/co/elastic/otel/android/compilation/tools/sourceheader/subplugins).

## Publishing

The APM Android Agent project has several modules needed to be deployed. Most of them are deployed
to [Maven Central](https://central.sonatype.com/), which are the regular Java/Android libraries, and the rest
are Gradle plugin modules, such as the `agent-plugin` and all of the `instrumentation/*/plugin`
ones, which will go into the [Gradle Plugin Portal](https://plugins.gradle.org/) instead.

Both kinds of deployments are automatically configured for all the modules available in this
project, this tool takes care of checking what are the types of the projects (either Java library,
Android library or Gradle plugin) and sets their deployment configuration accordingly.

### Publishing parameters

- `Group ID`: It comes from the root "gradle.properties" file, it's a property named `group`.
- `Artifact ID`: It's the module's dir name, for example, for the module `agent-sdk`, its artifact
  id will be `agent-sdk`.
- `Version`: It comes from the root "gradle.properties" file, it's a property named `version`.

### Publishing to Maven Central

The Maven Central deployment is configured using the
official [Maven Publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html),
and the deploy task is handled by
the [Gradle Maven publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin/).

#### Requirements

Before executing the publishing command, the following signing environment variables must be set:

- ORG_GRADLE_PROJECT_signingKey
- ORG_GRADLE_PROJECT_signingPassword

More info about those variables
can be found [here](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys).

As well as the following Sonatype credentials env vars:

- ORG_GRADLE_PROJECT_mavenCentralUsername
- ORG_GRADLE_PROJECT_mavenCentralUsername

More info on those, [here](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)

#### Triggering the deploy

After the requirements are set up, the command needed to deploy all the non-gradle-plugin modules
from this project is:

```text
./gradlew publishAndReleaseElasticToMavenCentral -Prelease=true
```

### Publishing to the Gradle Plugin Portal

The module `agent-plugin` and also all the `instrumentation/*/plugin` ones, need to go to the Gradle Plugin Portal, instead of Maven Central so
that it can be applied easily into gradle projects using
the [Gradle Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block),
which is the current way of doing so, otherwise people would have to apply our plugin using
the [legacy way](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application).
This tool already takes care of configuring
[all the parameters needed for the deploy](https://plugins.gradle.org/docs/publish-plugin), but
there's a couple of requirements needed prior to the publishing.

#### Requirements

This requirement is needed to be done only once, which is that a Gradle Plugin Portal account must
be set up,
[as explained here](https://docs.gradle.org/7.4/userguide/publishing_gradle_plugins.html#create_an_account_on_the_gradle_plugin_portal)
. This process will provide to values:

- A publish key.
- A publish secret.

Needed to be passed later into the publishing command.

#### Triggering the deploy

After the requirements are set up, the command needed to deploy all the gradle-plugin modules from
this project is:

```text
./gradlew publishPlugins -Pgradle.publish.key=<key> -Pgradle.publish.secret=<secret> -Prelease=true
```

