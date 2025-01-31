package co.elastic.otel.android.test

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import co.elastic.otel.android.api.ElasticOtelAgent
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AgentRule : TestRule, InstrumentationApplication.Callback {
    lateinit var agentInitializer: ((Application) -> ElasticOtelAgent)
    private var agent: ElasticOtelAgent? = null

    fun getAgent(): ElasticOtelAgent {
        return agent!!
    }

    override fun apply(base: Statement, description: Description): Statement {
        val application =
            InstrumentationRegistry.getInstrumentation().context as InstrumentationApplication
        application.callback = this

        try {
            return object : Statement() {
                override fun evaluate() {
                    base.evaluate()
                }
            }
        } finally {
            agent = null
        }
    }

    override fun onCreate(application: Application) {
        agent = agentInitializer.invoke(application)
    }
}