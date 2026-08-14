---
navigation_title: Crash reporting
description: Capture Android crashes and deobfuscate R8 stacktraces with EDOT Android.
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

# Report Android crashes

EDOT Android can capture unhandled exceptions and report them to your {{stack}} as crash events. Each event includes exception details, a stacktrace, and [session](index.md#sessions) information that helps correlate the crash with other telemetry from the application.

## Enable crash reporting

First, [set up EDOT Android](getting-started.md#gradle-setup) in your application. Then add the crash reporting instrumentation plugin to the application module's `build.gradle.kts` file:

```kotlin
plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent") version "[latest_version]"
    id("co.elastic.otel.android.instrumentation.crash") version "[latest_version]"
}
```

Find the latest crash reporting plugin version in the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.crash).

The instrumentation automatically captures crashes when an unhandled exception occurs. Because the application process is terminating, EDOT Android stores the crash event on disk and exports it after EDOT Android initializes the next time the application is launched.

Crash events are available in the "Crashes" section of the {{kib}} Android dashboard. Refer to [Visualize your telemetry](getting-started.md#visualize-telemetry) for instructions to install and open the dashboard.

## Deobfuscate R8 stacktraces

```{applies_to}
product:
  edot_android: ga 1.8.0
```

Android's R8 optimizer can rename classes and methods in release builds. When an optimized application crashes, its stacktrace contains these obfuscated names. EDOT Android can upload the R8 `mapping.txt` file for each application build to {{es}} so that the stacktrace can be restored to its original class, method, file, and line information.

EDOT Android identifies the correct mapping using the `app.build_id` resource attribute included with application telemetry. Mapping documents for a build are stored in a {{es}} index named `.android-r8-mappings-<build_id>`.

Deobfuscating stacktraces in {{kib}} requires version 1.0.0 or later of the [Android OpenTelemetry Assets](https://www.elastic.co/docs/reference/integrations/otel_android_dashboards) integration. Refer to the integration page for the minimum supported {{kib}} versions.

### Prerequisites

* Enable R8 for the variant whose mapping you want to upload.
* Have your {{es}} endpoint URL at hand to use it in the Gradle task later.
* Create a dedicated {{es}} API key for mapping uploads.
* Install version 1.0.0 or later of the [Android OpenTelemetry Assets](https://www.elastic.co/docs/reference/integrations/otel_android_dashboards) integration in {{kib}}. The integration page lists the minimum supported {{kib}} versions. Refer to [Visualize telemetry](getting-started.md#visualize-telemetry) for installation instructions.

### Create a dedicated API key

Create an API key specifically for R8 mapping uploads. Don't reuse the API key that the application uses to export telemetry. Keeping these credentials separate limits the impact if a build-time credential is exposed and allows each key to be rotated or revoked independently.

The mapping uploader only needs permission to create mapping indices and index documents into them. Create a least-privilege key in {{kib}}:

1. Open the **API keys** management page using the navigation menu or the [global search field](docs-content://explore-analyze/find-and-organize/find-apps-and-objects.md).
2. Select **Create API key** and give the key a descriptive name, such as `edot-android-r8-mapping-upload`.
3. Select **Control security privileges**.
4. Use the following role descriptor to restrict the key to mapping uploads:

```json
{
  "r8_mapping_uploader": {
    "indices": [
      {
        "names": [".android-r8-mappings-*"],
        "privileges": ["create_index", "index"]
      }
    ]
  }
}
```

5. Set an expiration that matches your release process, create the key, and store its encoded value as the `ELASTICSEARCH_API_KEY` CI secret.

This API key is a build-time credential. Don't package it in the application or commit it to source control.

The user creating the key must have API key creation permissions and must already hold the index privileges being granted. Refer to [Elastic API keys](docs-content://deploy-manage/api-keys/elasticsearch-api-keys) for deployment-specific requirements and instructions.

### Configure mapping uploads

Add the R8 mapping plugin to the application module's `build.gradle.kts` file. Use the same version as the EDOT Android agent plugin:

```kotlin
plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent") version "[latest_version]"
    id("co.elastic.otel.android.mapping") version "[latest_version]"
}
```

Find the latest mapping plugin version in the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/co.elastic.otel.android.mapping).

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
:::{note}
The endpoint must be the {{es}} HTTP endpoint, not the OTLP endpoint used to export telemetry.
:::

### Build ID

By default, EDOT Android generates the build ID as:

```text
sha256("<applicationId>-<versionName>-<versionCode>")
```

The agent adds this value to telemetry as `app.build_id`, and the mapping plugin uses the same value in the mapping index name. Increment the application's version code for each release so that every distinct application binary has a unique build ID.

### Upload a mapping file

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

1. Ensures R8 has produced `mapping.txt` for the selected variant.
2. Converts the mapping into one {{es}} document per obfuscated class.
3. Creates `.android-r8-mappings-<build_id>` if it doesn't already exist.
4. Uploads the documents using the {{es}} Bulk API.

Mapping upload is manual and isn't attached to `assemble` or `bundle`. Run the upload task in the release workflow for every optimized application binary that you distribute.

Uploading the same build again updates documents with deterministic IDs instead of creating duplicates.

### Visualize deobfuscated stacktraces

After the crash event and its corresponding R8 mapping are available in your {{stack}}, you can retrace the stacktrace from the {{kib}} Android dashboard. Refer to [Deobfuscating stacktraces](https://www.elastic.co/docs/reference/integrations/otel_android_dashboards#deobfuscating-stacktraces) for instructions to open the crash details and view the original class names, methods, source files, and line numbers.

### Troubleshoot mapping uploads

**No mapping file is generated**
: Confirm that R8 is enabled for the selected variant with `isMinifyEnabled = true`, and invoke the task for that variant.

**{{es}} rejects the request**
: Confirm that the endpoint is the {{es}} HTTP endpoint and that the API key can create and write to `.android-r8-mappings-*` indices.

**The mapping can't be associated with a crash**
: Confirm that the mapping index suffix matches the crash event's `app.build_id`.
