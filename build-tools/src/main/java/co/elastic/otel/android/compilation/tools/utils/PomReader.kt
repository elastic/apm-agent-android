package co.elastic.otel.android.compilation.tools.utils

import java.io.File
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader

class PomReader(pomFile: File) {
    private val model: Model = MavenXpp3Reader().read(pomFile.inputStream())

    fun getLicenseId(): String? {
        val licenses = model.licenses
        if (licenses.size == 0) {
            return null
        }

        val license = licenses.first()
        val name = license.name
        val id =
            LicensesIdsMatcher.findId(name)
                ?: throw RuntimeException("Couldn't find a license id for: $name - it should be added to the 'licenses_ids.txt' file")
        return id
    }

    fun getName(): String? = model.name

    fun getUrl(): String? = model.url
}
