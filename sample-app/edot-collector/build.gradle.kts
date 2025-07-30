import de.undercouch.gradle.tasks.download.DownloadExtension
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import org.apache.commons.lang3.SystemUtils

plugins {
    id("de.undercouch.download") version "5.6.0"
}

val coordinates = getEdotCoordinates()

val findEdotCollector = tasks.register<FindEdotCollectorVersion>("findEdotCollectorVersion") {
    group = "edot"
    outputFile.set(project.layout.buildDirectory.file("version.properties"))
}

val downloadEdotCollector =
    tasks.register<DownloadEdotCollector>("downloadEdotCollector", coordinates, download)
downloadEdotCollector.configure {
    group = "edot"
    versionPropertiesFile.set(findEdotCollector.flatMap { it.outputFile })
    outputEdotCollectorDir.set(project.layout.buildDirectory.dir("bin"))
    downloadedArchive.set(
        project.layout.buildDirectory.file(
            "intermediate/$name/release.${coordinates.fileFormat}"
        )
    )
}

val createConfigFile = tasks.register<CreateEdotConfigurationTask>("createEdotConfiguration") {
    templateFile.set(project.layout.projectDirectory.file("templates/edot_configuration_template.yml"))
    elasticsearchPropertiesFile.set(project.layout.projectDirectory.file("elasticsearch.properties"))
    configFile.set(project.layout.buildDirectory.file("configuration/edot_configuration.yml"))
}

abstract class FindEdotCollectorVersion : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        // Get the latest release tag by following the redirect from GitHub's latest release URL
        val latestReleaseUrl = "https://github.com/elastic/elastic-agent/releases/latest"
        val connection = URL(latestReleaseUrl).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = "HEAD"

        val redirectLocation = connection.getHeaderField("Location")
        connection.disconnect()

        // Extract tag from URL like: https://github.com/elastic/elastic-agent/releases/tag/v9.0.3
        val versionPattern = Regex("v((?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*))")
        val version = versionPattern.find(redirectLocation)?.groupValues?.get(1)
            ?: throw IllegalStateException("Could not find version in '$redirectLocation'")

        logger.info("Found EDOT Collector version: '{}'", version)

        outputFile.get().asFile.writeText("version=$version")
    }
}

abstract class DownloadEdotCollector @Inject constructor(
    private val edotCoordinates: EdotCoordinates,
    private val download: DownloadExtension,
    private val archiveOps: ArchiveOperations,
    private val fileOps: FileSystemOperations,
) : DefaultTask() {

    @get:OutputDirectory
    abstract val outputEdotCollectorDir: DirectoryProperty

    @get:InputFile
    abstract val versionPropertiesFile: RegularFileProperty

    @get:Internal
    abstract val downloadedArchive: RegularFileProperty

    @TaskAction
    fun execute() {
        val version: String = (Properties().apply {
            versionPropertiesFile.get().asFile.inputStream().use {
                load(it)
            }
        }["version"] ?: throw IllegalStateException("Could not find version")) as String
        val name =
            "elastic-agent-$version-${edotCoordinates.os.value}-${edotCoordinates.arch.value}"

        download.run {
            src(getUrl(name))
            dest(downloadedArchive)
        }
        val edotExpanded =
            if (edotCoordinates.fileFormat == "zip") archiveOps.zipTree(downloadedArchive) else archiveOps.tarTree(
                downloadedArchive
            )
        fileOps.sync {
            from(edotExpanded)
            include("$name/**")
            eachFile {
                relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
            }
            includeEmptyDirs = false
            into(outputEdotCollectorDir)
        }
    }

    private fun getUrl(name: String): String {
        return "https://artifacts.elastic.co/downloads/beats/elastic-agent/$name.${edotCoordinates.fileFormat}"
    }
}

abstract class CreateEdotConfigurationTask : DefaultTask() {
    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:InputFile
    abstract val elasticsearchPropertiesFile: RegularFileProperty

    @get:OutputFile
    abstract val configFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val properties = Properties().apply {
            elasticsearchPropertiesFile.get().asFile.inputStream().use {
                load(it)
            }
        }
        var text = templateFile.get().asFile.readText()
        text = text.replace("ELASTIC_ENDPOINT", properties.getProperty("endpoint"))
        text = text.replace("ELASTIC_API_KEY", properties.getProperty("api_key"))

        configFile.get().asFile.writeText(text)
    }
}

abstract class RunEdotCollectorTask @Inject constructor(
    private val execOperations: ExecOperations,
    private val edotCoordinates: EdotCoordinates
) : DefaultTask() {
    @get:InputDirectory
    abstract val edotDir: DirectoryProperty

    @get:InputFile
    abstract val configFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val scriptFileFormat = if (edotCoordinates.os == Os.WINDOWS) ".ps1" else ""
        val scriptFile = File(edotDir.get().asFile, "otelcol$scriptFileFormat")
        val args = "--config ${configFile.get().asFile.absolutePath}"
        if (edotCoordinates.os == Os.WINDOWS) {
            execOperations.exec {
                commandLine(
                    "powershell -noexit -executionpolicy bypass -File ${scriptFile.absolutePath}",
                    args
                )
            }
        } else {
            execOperations.exec {
                commandLine(scriptFile.absolutePath, args)
            }
        }
    }
}

fun getEdotCoordinates(): EdotCoordinates {
    val os = getOs()
    val arch = getArch()
    logger.info("Found OS '{}' and arch: '{}'", os, arch)
    return EdotCoordinates(os, arch, getFileFormat(os))
}

fun getOs(): Os {
    return when {
        SystemUtils.IS_OS_LINUX -> Os.LINUX
        SystemUtils.IS_OS_MAC -> Os.MAC
        SystemUtils.IS_OS_WINDOWS -> Os.WINDOWS
        else -> throw UnsupportedOperationException("OS not identified: ${SystemUtils.OS_NAME}")
    }
}

fun getArch(): Arch {
    val archName = SystemUtils.OS_ARCH
    var arch: Arch? = null
    if (archName == "aarch64") {
        arch = Arch.AARCH64
    }
    if (archName.contains("arm")) {
        arch = Arch.AARCH64
    }
    if (arch == null) {
        arch = Arch.X86_64
    }
    logger.info("Providing arch: '{}' for name: '{}'", arch, archName)
    return arch
}

fun getFileFormat(os: Os) = if (os == Os.WINDOWS) "zip" else "tar.gz"

enum class Arch(val value: String) {
    X86_64("x86_64"),
    AARCH64("aarch64")
}

enum class Os(val value: String) {
    LINUX("linux"),
    MAC("darwin"),
    WINDOWS("windows")
}

data class EdotCoordinates(val os: Os, val arch: Arch, val fileFormat: String)