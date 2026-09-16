import com.android.build.api.variant.HasHostTestsBuilder
import com.android.build.api.variant.HostTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("elastic.animalsniffer-android")
    id("elastic.dependency-floors")
}

val javaVersionStr = project.property("elastic.java.compatibility") as String
val sdkCompileVersion = (project.property("elastic.android.compileSdk") as String).toInt()
android {
    compileSdk = sdkCompileVersion

    defaultConfig {
        minSdk = (project.property("elastic.android.minSdk") as String).toInt()
        consumerProguardFiles.add(rootProject.file("shared-rules.pro"))
    }

    val javaVersion = JavaVersion.toVersion(javaVersionStr)
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    lint {
        disable.add("NewApi")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersionStr)
        freeCompilerArgs.add("-jvm-default=no-compatibility")
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
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findBundle("junit").get())
    testImplementation(libs.findLibrary("assertj").get())
    testImplementation(project(":internal-tools:test-common"))
    testRuntimeOnly(libs.findLibrary("junit5-vintage").get())
}