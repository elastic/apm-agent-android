import java.util.Properties

val agentProperties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    agentProperties.load(it)
}
val agentVersion = agentProperties["version"]

val instrumentationProjectPattern = Regex(":instrumentation:([^:]+)$")
subprojects {
    instrumentationProjectPattern.matchEntire(path)?.let {
        val moduleId = it.groupValues[1]
        configurations.all {
            resolutionStrategy.dependencySubstitution {
                substitute(module("co.elastic.otel.android.instrumentation:${moduleId}-library"))
                    .using(module("co.elastic.otel.android.${moduleId}:library:$agentVersion"))
            }
            resolutionStrategy.dependencySubstitution {
                substitute(module("co.elastic.otel.android.instrumentation:${moduleId}-bytebuddy"))
                    .using(module("co.elastic.otel.android.${moduleId}:bytebuddy:$agentVersion"))
            }
        }
    }
}