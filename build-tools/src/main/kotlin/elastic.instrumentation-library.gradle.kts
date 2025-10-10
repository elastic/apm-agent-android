plugins {
    id("elastic.android-library")
    id("kotlin-kapt")
}

val instrumentationGroupId = "${rootProject.group}.instrumentation"

android {
    buildFeatures.buildConfig = true
    defaultConfig {
        val name = project.parent!!.name
        buildConfigField("String", "INSTRUMENTATION_ID", "\"${instrumentationGroupId}.$name\"")
        buildConfigField("String", "INSTRUMENTATION_VERSION", "\"$version\"")
    }
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(project(":instrumentation:api"))
    compileOnly(libs.findLibrary("autoService-annotations").get())
    kapt(libs.findLibrary("autoService-compiler").get())
}