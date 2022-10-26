# Publishing tools for the APM Android project

This is a Gradle plugin that provides several tools needed at compile time on the APM Android
project. In order to apply this plugin, the following must be added into the root
project `build.gradle` file:

```groovy
plugins {
    id 'co.elastic.apm.publishing'
}
```

The tools applied then are the following:

## Creating NOTICE files

Each subproject will get a new gradle task named `createNoticeFile` which takes care of gathering
all of its direct dependencies, then checking their POM files looking for their licenses, and then
it looks at their artifact files looking for NOTICE files to be merged (if any). Once all this
information is gathered, the subproject NOTICE file is generated and placed
in `subproject-dir/src/main/resources/META-INF/NOTICE`.

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

> The license ID must be available within this tool's `resources/licenses_ids.txt` file. More info on
> [How to add licenses IDs](docs/adding-license-ids.md).

Example of the contents of a file with two mappings:

```text
org.slf4j:slf4j-api:2.0.0|mit
com.squareup.okhttp3:okhttp:3.11.0|apache_v2
```

The "manually provided licenses" file must be set within the `build.gradle` file where the
dependencies being mapped to their licenses are defined. The way to reference the mappings file is
as shown below:

```groovy
// build.gradle containing the dependencies that need manual mapping.
licensesConfig {
    manualMappingFile = "path/to/the/mappings/file.txt"
}
```

## Adding source headers

This work is triggered when building this project so there's nothing manual to be done about it. The
source headers are added using [this tool](https://github.com/diffplug/spotless) which is configured
for both `java` and `kotlin` source files
in [here](src/main/java/co/elastic/apm/compile/tools/sourceheader/subplugins).

## Publishing

The APM Android Agent project has several modules needed to be deployed. All of them, except for
one, need to be deployed to maven central. The only exception is for the `android-plugin` module
which has to go into the [Gradle Plugin Portal](https://plugins.gradle.org/).

Both kinds of deployments are automatically configured for all the modules available in this
project, this tool takes care of checking what are the types of the projects (either Java library,
Android library or Gradle plugin) and sets their deployment configuration accordingly.

### Publishing to Maven Central

The Maven Central (Sonatype OSSRH Nexus) deployment is configured using the
official [Maven Publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html),
and the deploy task is handled by
the [Gradle Nexus publish plugin](https://github.com/gradle-nexus/publish-plugin).

#### Requirements

Before executing the publishing command, the following signing environment variables must be set:

- ORG_GRADLE_PROJECT_signingKey
- ORG_GRADLE_PROJECT_signingPassword

More info about those variables
in [here](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys).

As well as the following Sonatype credentials env vars:

- ORG_GRADLE_PROJECT_sonatypeUsername
- ORG_GRADLE_PROJECT_sonatypePassword

More info on those, [here](https://github.com/gradle-nexus/publish-plugin)

#### Triggering the deploy

After the requirements are set up, the command needed to deploy all the non-gradle-plugin modules
from this project is:

```text
./gradlew publishElasticPublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository
```

### Publishing to the Gradle Plugin Portal

The module `android-plugin` needs to go to the Gradle Plugin Portal, instead of Maven Central so
that it can be applied easily into gradle projects. This tool already takes care of configuring
[all the parameters needed for the deploy](https://plugins.gradle.org/docs/publish-plugin), but
there's a couple of requirements needed prior to the publishing.

#### Requirements

This requirement is needed to be done only once, which is that a Gradle Plugin Portal account must
be set up, [as explained here](https://docs.gradle.org/7.4/userguide/publishing_gradle_plugins.html)
. This process will provide to values:

- A publish key.
- A publish secret.

Needed to be passed later into the publishing command.

#### Triggering the deploy

After the requirements are set up, the command needed to deploy all the gradle-plugin modules from
this project is:

```text
./gradlew publishPlugins -Pgradle.publish.key=<key> -Pgradle.publish.secret=<secret>
```