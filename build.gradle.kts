plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    id("co.elastic.otel.publishing")
}
