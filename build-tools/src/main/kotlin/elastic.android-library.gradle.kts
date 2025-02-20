import com.android.build.api.variant.HasHostTestsBuilder
import com.android.build.api.variant.HostTestBuilder

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("elastic.animalsniffer-android")
}

android {
    compileSdk = (project.property("elastic.android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (project.property("elastic.android.minSdk") as String).toInt()
        consumerProguardFiles.add(rootProject.file("shared-rules.pro"))
    }

    val javaVersionStr = project.property("elastic.java.compatibility") as String
    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersionStr
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
    lint {
        disable.add("NewApi")
    }
}

androidComponents.beforeVariants {
    if (it.buildType?.equals("debug") == false) {
        (it as HasHostTestsBuilder).hostTests.get(HostTestBuilder.UNIT_TEST_TYPE)?.enable = false
    }
}

tasks.withType(Test::class).configureEach {
    useJUnitPlatform()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    implementation(project(":agent-common"))
    testImplementation(libs.findBundle("mocking").get())
    testImplementation(libs.findBundle("junit").get())
    testImplementation(libs.findLibrary("assertj").get())
    testImplementation(project(":internal-tools:test-common"))
    testRuntimeOnly(libs.findLibrary("junit5-vintage").get())
}