plugins {
    id("elastic.android-library")
    id("kotlin-kapt")
}

android {
    namespace = "co.elastic.otel.android.instrumentation.launchtime"
}

dependencies {
    api(project(":instrumentation:api"))
    implementation(instrumentation.androidx.lifecycle.process)
    compileOnly(libs.autoService.annotations)
    kapt(libs.autoService.compiler)
}