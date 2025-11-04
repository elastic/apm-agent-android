package co.elastic.otel.android.test.rule

import android.annotation.SuppressLint
import android.app.Application
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry

class AndroidTestAgentRule : AgentRule() {

    @SuppressLint("RestrictedApi")
    override fun runInitialization(initialization: () -> Unit) {
        runOnUiThread {
            initialization()
        }
    }

    override fun getApplication(): Application {
        return InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    }
}