package co.elastic.apm.android.sdk.internal.services.re

import android.app.Application
import co.elastic.apm.android.sdk.internal.services.re.appinfo.AppInfoService
import co.elastic.apm.android.sdk.internal.services.re.preferences.PreferencesService
import java.io.Closeable

class ServiceManager(
    private val services: Map<Class<out Service>, Service>
) : Closeable {

    init {
        services.values.forEach { it.start() }
    }

    fun getPreferencesService(): PreferencesService {
        return getService(PreferencesService::class.java)
    }

    fun getAppInfoService(): AppInfoService {
        return getService(AppInfoService::class.java)
    }

    override fun close() {
        services.values.forEach { it.stop() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : Service> getService(type: Class<S>): S {
        return services.getValue(type) as S
    }

    companion object {
        fun create(application: Application): ServiceManager {
            val services = listOf(
                PreferencesService(application),
                AppInfoService(application)
            )
            return ServiceManager(services.associateBy { it.javaClass })
        }
    }
}