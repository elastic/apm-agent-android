import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

val javaVersionStr = project.property("elastic.java.compatibility") as String
val javaVersion = JavaVersion.toVersion(javaVersionStr)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin.compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(javaVersionStr))
    freeCompilerArgs = listOf("-Xjvm-default=all")
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}
tasks.withType(Javadoc::class).configureEach {
    enabled = false
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    testImplementation(libs.findBundle("mocking").get())
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findBundle("junit").get())
    testImplementation(libs.findLibrary("assertj").get())
    testRuntimeOnly(libs.findLibrary("junit5-vintage").get())
}