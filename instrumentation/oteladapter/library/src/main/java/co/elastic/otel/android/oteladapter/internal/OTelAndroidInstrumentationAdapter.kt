package co.elastic.otel.android.oteladapter.internal

import android.app.Application
import co.elastic.otel.android.api.ElasticOtelAgent
import co.elastic.otel.android.instrumentation.generated.oteladapter.BuildConfig
import co.elastic.otel.android.instrumentation.internal.Instrumentation
import io.opentelemetry.android.instrumentation.AndroidInstrumentationLoader
import io.opentelemetry.android.instrumentation.InstallationContext
import io.opentelemetry.android.session.SessionManager
import io.opentelemetry.android.session.SessionObserver

class OTelAndroidInstrumentationAdapter : Instrumentation {

    override fun install(application: Application, agent: ElasticOtelAgent) {
        val installationContext = InstallationContext(
            application,
            agent.getOpenTelemetry(),
            SESSION_MANAGER_NOOP
        )

        for (androidInstrumentation in AndroidInstrumentationLoader.get().getAll()) {
            androidInstrumentation.install(
                installationContext
            )
        }
    }

    override fun getId(): String {
        return BuildConfig.INSTRUMENTATION_ID
    }

    override fun getVersion(): String {
        return BuildConfig.INSTRUMENTATION_VERSION
    }

    companion object {
        private val SESSION_MANAGER_NOOP = object : SessionManager {
            override fun addObserver(observer: SessionObserver) {
                // No-op
            }

            override fun getSessionId(): String = ""
        }
    }
}