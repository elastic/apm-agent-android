plugins {
    id("java")
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val elasticApmVersion = "1.35.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("co.elastic.apm:apm-agent-attach:$elasticApmVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}
