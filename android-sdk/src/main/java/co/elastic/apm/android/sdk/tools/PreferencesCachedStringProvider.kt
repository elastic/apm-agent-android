package co.elastic.apm.android.sdk.tools

import co.elastic.apm.android.sdk.internal.services.Service
import co.elastic.apm.android.sdk.internal.services.ServiceManager
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService

class PreferencesCachedStringProvider(
    private val key: String,
    private val provider: StringProvider
) : CacheHandler<String>, StringProvider {
    private val preferences: PreferencesService by lazy {
        ServiceManager.get().getService(Service.Names.PREFERENCES)
    }

    override fun retrieve(): String? {
        return preferences.retrieveString(key)
    }

    override fun clear() {
        preferences.store(key, null)
    }

    override fun store(value: String) {
        preferences.store(key, value)
    }

    override fun get(): String {
        val retrieved = retrieve()
        if (retrieved != null) {
            return retrieved
        }

        val computed = provider.get()

        store(computed)

        return computed
    }
}