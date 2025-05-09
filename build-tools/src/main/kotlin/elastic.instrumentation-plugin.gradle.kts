import com.github.gmazzo.buildconfig.BuildConfigExtension

plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

val instrumentationGroupId = "${rootProject.group}.instrumentation"
val parentName = project.parent!!.name

buildConfig {
    packageName("${instrumentationGroupId}.generated.$parentName")
}

abstract class ElasticBuildConfig @Inject constructor(
    private val buildConfigExtension: BuildConfigExtension,
    private val buildConfigGroupId: String,
    private val parentProjectName: String,
    private val projectVersion: String
) {
    fun libraryUri() {
        dependencyUri(
            "LIBRARY_URI",
            "${buildConfigGroupId}:${parentProjectName}-library:$projectVersion"
        )
    }

    fun byteBuddyPluginUri() {
        dependencyUri(
            "BYTEBUDDY_PLUGIN_URI",
            "${buildConfigGroupId}:${parentProjectName}-bytebuddy:$projectVersion"
        )
    }

    fun dependencyUri(fieldName: String, dependencyUri: String) {
        buildConfigExtension.buildConfigField(
            "String",
            fieldName,
            """"$dependencyUri""""
        )
    }
}

abstract class InstrumentationPluginConfig @Inject constructor(
    private val gradlePlugin: GradlePluginDevelopmentExtension,
    private val projectDescription: String,
    private val parentProjectName: String
) {
    fun create(
        id: String = parentProjectName,
        extraTags: List<String> = emptyList(),
        action: Action<PluginDeclaration>
    ) {
        val pluginDeclaration = gradlePlugin.plugins.create("${id}Instrumentation")
        pluginDeclaration.id = "co.elastic.otel.android.instrumentation.$id"
        pluginDeclaration.description = projectDescription
        pluginDeclaration.tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        pluginDeclaration.tags.addAll(extraTags)
        action.execute(pluginDeclaration)
    }
}

project.extensions.create(
    "elasticBuildConfig",
    ElasticBuildConfig::class,
    buildConfig,
    instrumentationGroupId,
    parentName,
    version
)

project.extensions.create(
    "elasticInstrumentationPlugins",
    InstrumentationPluginConfig::class,
    gradlePlugin,
    project.description!!,
    parentName
)

dependencies {
    implementation(project(":agent-plugin"))
    implementation(project(":agent-api"))
}