import java.util.Properties

plugins {
    id("com.android.application")
    id("elastic.latest-dependency-resolution")
}

val properties = Properties()
val propertiesFile = File(rootDir, "../gradle.properties")
propertiesFile.inputStream().use {
    properties.load(it)
}

android {
    compileSdk = (properties.getProperty("elastic.android.compileSdk") as String).toInt()
}
