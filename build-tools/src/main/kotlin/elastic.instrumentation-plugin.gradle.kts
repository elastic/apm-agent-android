import com.github.gmazzo.buildconfig.BuildConfigExtension

plugins {
    id("elastic.java-library")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

val instrumentationGroupId = "${rootProject.group}.instrumentation"

buildConfig {
    packageName("${instrumentationGroupId}.generated")
}

abstract class ElasticBuildConfig @Inject constructor(
    private val buildConfigExtension: BuildConfigExtension,
    private val buildConfigGroupId: String,
    private val projectVersion: String
) {
    fun projectUri(fieldName: String, artifactName: String) {
        dependencyUri(fieldName, "${buildConfigGroupId}:$artifactName:$projectVersion")
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
    private val projectDescription: String
) {
    fun create(id: String, action: Action<PluginDeclaration>) {
        val pluginDeclaration = gradlePlugin.plugins.create("${id}Instrumentation")
        pluginDeclaration.id = "co.elastic.otel.android.instrumentation-$id"
        pluginDeclaration.description = projectDescription
        pluginDeclaration.tags.addAll("Android", "APM", "Elastic", "ELK", "opentelemetry")
        action.execute(pluginDeclaration)
    }
}

project.extensions.create(
    "elasticBuildConfig",
    ElasticBuildConfig::class,
    buildConfig,
    instrumentationGroupId,
    version
)

project.extensions.create(
    "elasticInstrumentationPlugins",
    InstrumentationPluginConfig::class,
    gradlePlugin,
    project.description!!
)

dependencies {
    implementation(project(":agent-plugin"))
    implementation(project(":agent-api"))
}