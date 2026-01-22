plugins {
    id("elastic.android-library")
    id("com.google.devtools.ksp")
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
    implementation(libs.findLibrary("autoService-annotations").get())
    ksp(libs.findLibrary("autoService-compiler").get())
}