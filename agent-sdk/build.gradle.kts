import de.undercouch.gradle.tasks.download.DownloadExtension
import java.net.HttpURLConnection
import java.net.URL

plugins {
    id("elastic.android-library")

    // For opamp:
    id("kotlin-kapt")
    id("de.undercouch.download") version "5.6.0"
    id("com.squareup.wire") version "5.3.3"
}

android {
    namespace = "co.elastic.otel.android"
    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "APM_AGENT_VERSION", "\"${project.version}\"")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("agent_version", project.version)
            }
        }
    }
}

apiValidation {
    ignoredClasses.add("co.elastic.otel.android.BuildConfig")
}

dependencies {
    api(project(":agent-api"))
    api(project(":instrumentation:api"))
    implementation(libs.opentelemetry.api.incubator)
    implementation(libs.stagemonitor.configuration)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.bundles.opentelemetry.semconv)
    implementation(libs.opentelemetry.diskBuffering)
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.core)
    implementation(libs.dsl.json)
    testImplementation(project(":internal-tools:otel-test-common"))
    testImplementation(libs.wireMock)
    testImplementation(libs.opentelemetry.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.awaitility)

    // For opamp:
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation(instrumentation.okhttp)
    implementation("com.github.f4b6a3:uuid-creator:6.0.0")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    kapt("com.google.auto.value:auto-value:1.11.0")
    compileOnly("com.google.auto.value:auto-value-annotations:1.11.0")
}

// For opamp:
apiValidation {
    ignoredPackages.add("opamp.proto")
}

val opampProtos = tasks.register<DownloadOpampProtos>("opampProtoDownload", download)
opampProtos.configure {
    group = "opamp"
    outputProtosDir.set(project.layout.buildDirectory.dir("opamp/protos"))
    downloadedZipFile.set(project.layout.buildDirectory.file("intermediate/$name/release.zip"))
}

wire {
    java {}
    sourcePath {
        srcDir(opampProtos)
    }
}

abstract class DownloadOpampProtos @Inject constructor(
    private val download: DownloadExtension,
    private val archiveOps: ArchiveOperations,
    private val fileOps: FileSystemOperations,
) : DefaultTask() {

    @get:OutputDirectory
    abstract val outputProtosDir: DirectoryProperty

    @get:Internal
    abstract val downloadedZipFile: RegularFileProperty

    @TaskAction
    fun execute() {
        // Get the latest release tag by following the redirect from GitHub's latest release URL
        val latestReleaseUrl = "https://github.com/open-telemetry/opamp-spec/releases/latest"
        val connection = URL(latestReleaseUrl).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = "HEAD"

        val redirectLocation = connection.getHeaderField("Location")
        connection.disconnect()

        // Extract tag from URL like: https://github.com/open-telemetry/opamp-spec/releases/tag/v0.12.0
        val latestTag = redirectLocation.substringAfterLast("/")
        // Download the source code for the latest release
        val zipUrl = "https://github.com/open-telemetry/opamp-spec/zipball/$latestTag"

        download.run {
            src(zipUrl)
            dest(downloadedZipFile)
        }
        val protos = archiveOps.zipTree(downloadedZipFile).matching {
            setIncludes(listOf("**/*.proto"))
        }
        fileOps.sync {
            from(protos.files)
            into(outputProtosDir)
        }
    }
}
