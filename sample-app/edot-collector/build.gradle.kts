import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.undercouch.gradle.tasks.download.DownloadExtension
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.Properties
import java.util.regex.Pattern
import kotlin.io.path.writeBytes
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.lang3.SystemUtils

plugins {
    id("de.undercouch.download") version "5.6.0"
}

val coordinates = getEdotCoordinates()

val findEdotCollectorVersion =
    tasks.register<FindEdotCollectorVersion>("findEdotCollectorVersion") {
        group = "edot"
        outputFile.set(project.layout.buildDirectory.file("version.properties"))
    }

val downloadEdotCollector =
    tasks.register<DownloadEdotCollector>("downloadEdotCollector", coordinates, download)
downloadEdotCollector.configure {
    group = "edot"
    versionPropertiesFile.set(findEdotCollectorVersion.flatMap { it.outputFile })
    outputEdotCollectorDir.set(project.layout.buildDirectory.dir("bin"))
    downloadedArchive.set(
        project.layout.buildDirectory.file(
            "intermediate/$name/release.${coordinates.fileFormat}"
        )
    )
}

val createConfigFile = tasks.register<CreateEdotConfigurationTask>("createEdotConfiguration") {
    group = "edot"
    templateFile.set(project.layout.projectDirectory.file("templates/edot-configuration-template.yml"))
    elasticsearchPropertiesFile.set(project.layout.projectDirectory.file("../elasticsearch.properties"))
    configFile.set(project.layout.buildDirectory.file("configuration/edot-configuration.yml"))
}

tasks.register("prepareEdotCollector") {
    group = "edot"
    dependsOn(downloadEdotCollector)
    dependsOn(createConfigFile)
}

abstract class FindEdotCollectorVersion : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val semverPattern = Regex("^\\d+\\.\\d+\\.\\d+\$")
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://artifacts.elastic.co/releases/stack.json"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val releases = moshi.adapter(Releases::class.java).fromJson(response.body())!!

        val validVersions = releases.releases.map { it.version }
            .filter { semverPattern.matches(it) }
        val versionFound = validVersions.max()

        logger.info("Found EDOT Collector version: '{}'", versionFound)

        outputFile.get().asFile.writeText("version=$versionFound")
    }

    data class Releases(val releases: List<Release>)

    data class Release(val version: String)
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

        val outputDir = outputEdotCollectorDir.get().asFile

        // Clean up
        outputDir.deleteRecursively()

        if (edotCoordinates.os == Os.WINDOWS) {
            fileOps.copy {
                from(archiveOps.zipTree(downloadedArchive))
                include("$name/**")
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }
                includeEmptyDirs = false
                into(outputEdotCollectorDir)
            }
        } else {
            untar(downloadedArchive.get().asFile, outputDir, name)
        }
    }

    private fun untar(archive: File, destinationDir: File, name: String) {
        val rootDirPattern = Regex("^" + Pattern.quote(name) + "\\/")
        val destinationDirPath = destinationDir.toPath()
        TarArchiveInputStream(
            GzipCompressorInputStream(
                archive.inputStream().buffered()
            )
        ).use { tar ->
            var entry = tar.nextEntry as TarArchiveEntry?
            while (entry != null) {
                if (entry.name == name) {
                    // Skip root dir
                    entry = tar.nextEntry as TarArchiveEntry?
                    continue
                }

                val curatedEntryName = entry.name.replace(rootDirPattern, "")
                val extractTo = destinationDirPath.resolve(curatedEntryName)

                if (entry.isSymbolicLink) {
                    logger.info("Found symlink: '$extractTo' with name: '${entry.linkName}'")
                    val linkTarget = destinationDirPath.resolve(entry.linkName)
                    ensureParentDirExists(extractTo)
                    Files.createSymbolicLink(extractTo, linkTarget)
                } else if (entry.isDirectory) {
                    Files.createDirectories(extractTo)
                } else {
                    ensureParentDirExists(extractTo)
                    Files.createFile(
                        extractTo,
                        PosixFilePermissions.asFileAttribute(
                            setOf(
                                PosixFilePermission.OWNER_READ,
                                PosixFilePermission.OWNER_WRITE,
                                PosixFilePermission.OWNER_EXECUTE,
                                PosixFilePermission.GROUP_READ,
                                PosixFilePermission.GROUP_EXECUTE,
                                PosixFilePermission.OTHERS_READ,
                                PosixFilePermission.OTHERS_EXECUTE
                            )
                        )
                    )
                    extractTo.writeBytes(tar.readAllBytes())
                }
                entry = tar.nextEntry as TarArchiveEntry?
            }
        }
    }

    private fun ensureParentDirExists(filePath: Path) {
        filePath.parent?.let {
            if (Files.notExists(it)) {
                Files.createDirectories(it)
            }
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

    companion object {
        private const val PROPERTY_ENDPOINT_PLACEHOLDER = "YOUR_ELASTICSEARCH_ENDPOINT"
        private const val PROPERTY_API_KEY_PLACEHOLDER = "YOUR_ELASTICSEARCH_API_KEY"
    }

    @TaskAction
    fun execute() {
        val properties = getElasticsearchProperties()
        var text = templateFile.get().asFile.readText()
        text = text.replace("ELASTIC_ENDPOINT", properties.endpoint)
        text = text.replace("ELASTIC_API_KEY", properties.apiKey)

        configFile.get().asFile.writeText(text)
    }

    private fun getElasticsearchProperties(): ElasticsearchProperties {
        val properties = Properties().apply {
            elasticsearchPropertiesFile.get().asFile.inputStream().use {
                load(it)
            }
        }

        val endpoint = properties.getProperty("endpoint")
        val apiKey = properties.getProperty("api_key")

        if (endpoint.isEmpty() || endpoint == PROPERTY_ENDPOINT_PLACEHOLDER) {
            throw IllegalArgumentException("You must provide your Elasticsearch endpoint in the elasticsearch.properties file.")
        }
        if (apiKey.isEmpty() || apiKey == PROPERTY_API_KEY_PLACEHOLDER) {
            throw IllegalArgumentException("You must provide your Elasticsearch apiKey in the elasticsearch.properties file.")
        }

        return ElasticsearchProperties(endpoint, apiKey)
    }

    data class ElasticsearchProperties(val endpoint: String, val apiKey: String)
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
