import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("elastic.java-library")
}

val javaVersionStr = project.property("elastic.plugin.java.compatibility") as String
val javaVersion = JavaVersion.toVersion(javaVersionStr)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin.compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(javaVersionStr))
}
