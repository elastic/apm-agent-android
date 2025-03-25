#!/usr/bin/env kotlin
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.3")
@file:DependsOn("com.squareup.moshi:moshi-kotlin:1.15.2")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReleaseUpdateItem(val message: String, val prId: String?)

data class ReleaseUpdates(
    @Json(name = "features_enhancements")
    val featuresEnhancements: List<ReleaseUpdateItem>?,
    val fixes: List<ReleaseUpdateItem>?
)

class GenerateReleaseNotesCommand : CliktCommand() {
    private val source: String by option().prompt("Source file path")
        .help("The release notes file in JSON format, check out sample.json")
    private val version: String by option().prompt("Release version")
        .help("The version associated to the release notes")

    override fun run() {
        print(buildUpdatesText(readJson()))
    }

    private fun buildUpdatesText(updates: ReleaseUpdates): String {
        val versionNumbers = version.replace(".", "")
        val textBuilder = StringBuilder(
            """
            ## $version [elastic-apm-android-agent-$versionNumbers-release-notes]
            **Release date:** ${SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())}
        """.trimIndent()
        )

        if (updates.featuresEnhancements != null) {
            textBuilder.appendLine().appendLine()
            textBuilder.append(
                """
            ### Features and enhancements [elastic-apm-android-agent-$versionNumbers-features-enhancements]
            """.trimIndent()
            )

            appendItems(textBuilder, updates.featuresEnhancements)
        }

        if (updates.fixes != null) {
            textBuilder.appendLine().appendLine()
            textBuilder.append(
                """
            ### Fixes [elastic-apm-android-agent-$versionNumbers-fixes]
            """.trimIndent()
            )

            appendItems(textBuilder, updates.fixes)
        }

        return textBuilder.toString()
    }

    private fun appendItems(builder: StringBuilder, items: List<ReleaseUpdateItem>) {
        builder.appendLine()
        items.forEach {
            builder.appendLine()
            builder.append(
                """
                * ${it.message}${if (it.prId != null) ": [#${it.prId}](https://github.com/elastic/apm-agent-android/pull/${it.prId})" else ""}
            """.trimIndent()
            )
        }
    }

    private fun readJson(): ReleaseUpdates {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        return moshi.adapter(ReleaseUpdates::class.java).fromJson(File(source).readText())!!
    }
}

GenerateReleaseNotesCommand().main(args)