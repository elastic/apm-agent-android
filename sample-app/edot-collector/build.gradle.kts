import de.undercouch.gradle.tasks.download.DownloadExtension
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import org.apache.commons.lang3.SystemUtils

plugins {
    id("de.undercouch.download") version "5.6.0"
}

val coordinates: EdotCoordinates = getEdotCoordinates()

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

        download.run {
            src(getUrl(version))
            dest(downloadedArchive)
        }
        val edotExpanded =
            if (edotCoordinates.fileFormat == "zip") archiveOps.zipTree(downloadedArchive) else archiveOps.tarTree(
                downloadedArchive
            )
        fileOps.sync {
            from(edotExpanded)
            into(outputEdotCollectorDir)
        }
    }

    private fun getUrl(version: String): String {
        return "https://artifacts.elastic.co/downloads/beats/elastic-agent/elastic-agent-$version-${edotCoordinates.os.value}-${edotCoordinates.arch.value}.${edotCoordinates.fileFormat}"
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