import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("co.elastic.otel.android.agent")
}

android {
    namespace = "co.elastic.otel.android.baseline"
    compileSdk = providers.gradleProperty("baseline.compileSdk")
        .map { it.toInt() }
        .getOrElse(36)

    defaultConfig {
        applicationId = "co.elastic.otel.android.baseline"
        minSdk = 26
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

// A published floor must be the highest version the published graph requires. Otherwise a
// consumer that supplies nothing else resolves above the declared floor, and the floor is a lie.
val expectedFloors = mapOf(
    "org.jetbrains.kotlin:kotlin-stdlib" to rootProject.extra["kotlinFloorVersion"] as String,
    "com.squareup.okhttp3:okhttp" to rootProject.extra["okhttpFloorVersion"] as String
)

fun ResolvedComponentResult.allEdges(): List<ResolvedDependencyResult> {
    val visited = mutableSetOf<ResolvedComponentResult>()
    val edges = mutableListOf<ResolvedDependencyResult>()
    fun walk(component: ResolvedComponentResult) {
        if (!visited.add(component)) return
        component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach {
            edges.add(it)
            walk(it.selected)
        }
    }
    walk(this)
    return edges
}

androidComponents {
    onVariants(selector().withName("release")) { variant ->
        val rootComponent = variant.runtimeConfiguration.incoming.resolutionResult.rootComponent
        tasks.register("verifyBaselineFloors") {
            group = "verification"
            description =
                "Fails when the resolved kotlin-stdlib or okhttp differs from its published floor."
            doLast {
                val edges = rootComponent.get().allEdges()
                val failures = expectedFloors.mapNotNull { (module, expected) ->
                    val (group, name) = module.split(":")
                    val selected = edges.filter {
                        it.selected.moduleVersion?.group == group &&
                            it.selected.moduleVersion?.name == name
                    }
                    when (val resolved = selected.firstOrNull()?.selected?.moduleVersion?.version) {
                        null -> "$module is missing from the release runtime classpath; floor is $expected"
                        expected -> null
                        else -> selected.joinToString(
                            prefix = "$module resolved $resolved but its published floor is $expected; requested by:\n",
                            separator = "\n"
                        ) { "  ${it.from.id} requested ${it.requested}" }
                    }
                }
                if (failures.isNotEmpty()) {
                    throw GradleException(failures.joinToString("\n"))
                }
                logger.lifecycle("Published floors hold: $expectedFloors")
            }
        }
    }
}
