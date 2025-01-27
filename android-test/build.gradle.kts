import java.io.File
import java.util.Properties

val agentProperties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    agentProperties.load(it)
}

extra.apply {
    set("jvmCompatibility", JavaVersion.VERSION_17)
    set("androidCompileSdk", 35)
    set("androidMinSdk", 26)
    set("agentVersion", agentProperties["version"])
}