plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("co.elastic.otel:elastic-otel-runtime-attach:1.5.0")
}