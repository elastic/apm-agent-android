plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("co.elastic.otel:elastic-otel-runtime-attach:1.5.0")
}