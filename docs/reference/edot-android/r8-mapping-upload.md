---
navigation_title: R8 mapping upload
description: Upload Android R8 mapping files to {{es}} for crash stacktrace deobfuscation.
applies_to:
  stack:
  serverless:
    observability:
  product:
    edot_android: ga
products:
  - id: cloud-serverless
  - id: observability
  - id: edot-sdk
---

# Upload R8 mappings for crash deobfuscation

Android's R8 optimizer can rename classes and methods in release builds. When an optimized application crashes, its stacktrace contains these obfuscated names. EDOT Android can upload the R8 `mapping.txt` file for each application build to {{es}} so that the stacktrace can be restored to its original class, method, file, and line information.

EDOT Android identifies the correct mapping using the `app.build_id` resource attribute included with application telemetry. Mapping documents for a build are stored in a {{es}} index named `.android-r8-mappings-<build_id>`.

## Prerequisites

Before uploading a mapping:

* [Set up EDOT Android](getting-started.md#gradle-setup) in your application.
* Enable the [crash reporting instrumentation](automatic-instrumentation.md#crash-reporting) to capture unhandled exceptions.
* Enable R8 for the variant whose mapping you want to upload.
* Make the {{es}} endpoint reachable from the environment that runs Gradle.
* Create a {{es}} API key that can create and write to `.android-r8-mappings-*` indices.

The {{es}} credentials used for mapping uploads are build-time credentials. Don't package them in the application or commit them to source control.

## Configure mapping uploads

Add the R8 mapping plugin to the application module's `build.gradle.kts` file. Use the same version as the EDOT Android agent plugin:

```kotlin
plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent") version "[latest_version]"
    id("co.elastic.otel.android.mapping") version "[latest_version]"
}
```

Configure the {{es}} endpoint and API key in the `elasticOtel` block. Gradle providers allow CI to supply these values without storing credentials in the build script:

```kotlin
elasticOtel {
    mapping {
        elasticsearch {
            endpoint.set(providers.environmentVariable("ELASTICSEARCH_ENDPOINT"))
            apiKey.set(providers.environmentVariable("ELASTICSEARCH_API_KEY"))
        }
    }
}

android {
    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
}
```

The endpoint must be the {{es}} HTTP endpoint, not the EDOT Collector OTLP endpoint used to export telemetry.

### Configure the build ID

By default, EDOT Android generates the build ID as:

```text
sha256("<applicationId>-<versionName>-<versionCode>")
```

The agent adds this value to telemetry as `app.build_id`, and the mapping plugin uses the same value in the mapping index name. You can instead set a [custom build ID](configuration.md#build-id):

```kotlin
elasticOtel {
    buildId.set("my-build-id")

    mapping {
        // ...
    }
}
```

Each distinct application binary must have a stable, unique build ID. If you use the generated default, increment the application's version code for each release.

## Upload a mapping

The plugin creates an upload task for every application variant. The task name follows this pattern:

```text
<variant>UploadMappingToElasticsearch
```

For the `release` variant, export the credentials and run:

```bash
export ELASTICSEARCH_ENDPOINT="https://your-elasticsearch-endpoint"
export ELASTICSEARCH_API_KEY="your-api-key"

./gradlew releaseUploadMappingToElasticsearch
```

For a flavored variant such as `paidRelease`, run:

```bash
./gradlew paidReleaseUploadMappingToElasticsearch
```

The upload task:

1. Runs R8 for the selected variant to produce `mapping.txt`.
2. Converts the mapping into one {{es}} document per obfuscated class.
3. Creates `.android-r8-mappings-<build_id>` if it doesn't already exist.
4. Uploads the documents using the {{es}} Bulk API.

Mapping upload is manual and isn't attached to `assemble` or `bundle`. Run the upload task in the release workflow for every optimized application binary that you distribute.

Uploading the same build again updates documents with deterministic IDs instead of creating duplicates.

## Verify an upload

Use the {{es}} API to confirm that the build's index exists and contains mapping documents:

```http
GET /_cat/indices/.android-r8-mappings-*?v
```

```http
GET /.android-r8-mappings-<build_id>/_count
```

The index suffix must match the `app.build_id` resource attribute on telemetry from the corresponding application binary.

## Troubleshoot mapping uploads

**No mapping file is generated**
: Confirm that R8 is enabled for the selected variant with `isMinifyEnabled = true`, and invoke the task for that variant.

**{{es}} rejects the request**
: Confirm that the endpoint is the {{es}} HTTP endpoint and that the API key can create and write to `.android-r8-mappings-*` indices.

**The mapping can't be associated with a crash**
: Confirm that the mapping index suffix matches the crash event's `app.build_id`. Avoid reusing a custom build ID for different application binaries.
