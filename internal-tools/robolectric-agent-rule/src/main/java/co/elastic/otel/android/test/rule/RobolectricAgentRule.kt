package co.elastic.otel.android.test.rule

import android.app.Application
import org.robolectric.RuntimeEnvironment

class RobolectricAgentRule : AgentRule() {

    override fun runInitialization(initialization: () -> Unit) {
        initialization()
    }

    override fun getApplication(): Application {
        return RuntimeEnvironment.getApplication()
    }
}