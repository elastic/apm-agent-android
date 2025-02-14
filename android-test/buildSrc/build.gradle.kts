import java.util.Properties

plugins {
    `kotlin-dsl`
}

val properties = Properties()
val propertiesFile = File(rootDir, "../../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

dependencies {
    implementation(rootLibs.kotlin.plugin)
    implementation(rootLibs.android.plugin)
}

