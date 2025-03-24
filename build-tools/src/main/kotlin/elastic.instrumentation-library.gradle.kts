plugins {
    id("elastic.android-library")
    id("kotlin-kapt")
    id("com.github.gmazzo.buildconfig")
}

val instrumentationGroupId = "${rootProject.group}.instrumentation"

buildConfig {
    packageName("${instrumentationGroupId}.generated")
    buildConfigField("INSTRUMENTATION_ID", "${instrumentationGroupId}.${project.parent!!.name}")
    buildConfigField("INSTRUMENTATION_VERSION", "$version")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(project(":instrumentation:api"))
    compileOnly(libs.findLibrary("autoService-annotations").get())
    kapt(libs.findLibrary("autoService-compiler").get())
}