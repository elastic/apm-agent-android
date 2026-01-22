import com.android.build.api.variant.HasHostTestsBuilder
import com.android.build.api.variant.HostTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("com.android.library")
    id("elastic.animalsniffer-android")
    id("elastic.common-dependency-conventions")
}

val javaVersionStr = project.property("elastic.java.compatibility") as String
val minKotlinVersionStr = project.property("elastic.kotlin.compatibility") as String
android {
    compileSdk = (project.property("elastic.android.compileSdk") as String).toInt()

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

val minKotlinVersion = KotlinVersion.fromVersion(minKotlinVersionStr)
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersionStr)
        apiVersion = minKotlinVersion
        languageVersion = minKotlinVersion
        freeCompilerArgs = listOf("-Xjvm-default=all")
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