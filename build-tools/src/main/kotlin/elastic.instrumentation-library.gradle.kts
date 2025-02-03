plugins {
    id("elastic.android-library")
    id("kotlin-kapt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    api(project(":instrumentation:api"))
    compileOnly(libs.findLibrary("autoService-annotations").get())
    kapt(libs.findLibrary("autoService-compiler").get())
}