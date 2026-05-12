import java.util.Locale
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSniffer

plugins {
    id("com.android.library")
    id("ru.vyarus.animalsniffer")
}

dependencies {
    signature("com.toasttab.android:gummy-bears-api-${project.property("elastic.android.minSdk")}:0.14.0:coreLib2@signature")
}

val capitalizedVariantNames = mutableListOf<String>()
androidComponents.onVariants { variant ->
    val capitalizedName =
        variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    capitalizedVariantNames.add(capitalizedName)
}

afterEvaluate {
    capitalizedVariantNames.forEach { capitalizedName ->
        tasks.named("pre${capitalizedName}Build").configure {
            finalizedBy("animalsniffer$capitalizedName")
        }
    }
}

// Any class, method or field annotated with this annotation will be ignored by animal sniffer.
animalsniffer.annotation = "androidx.annotation.RequiresApi"

tasks.withType<AnimalSniffer> {
    // always having declared output makes this task properly participate in tasks up-to-date checks
    reports.text.required.set(true)
}