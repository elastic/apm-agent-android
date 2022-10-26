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