plugins {
    id("elastic.android-library")
    id("kotlin-kapt")
    id("com.github.gmazzo.buildconfig")
}

val instrumentationGroupId = "${rootProject.group}.instrumentation"

buildConfig {
    val name = project.parent!!.name
    packageName("${instrumentationGroupId}.generated.$name")
    buildConfigField("INSTRUMENTATION_ID", "${instrumentationGroupId}.$name")
    buildConfigField("INSTRUMENTATION_VERSION", "$version")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(project(":instrumentation:api"))
    compileOnly(libs.findLibrary("autoService-annotations").get())
    kapt(libs.findLibrary("autoService-compiler").get())
}