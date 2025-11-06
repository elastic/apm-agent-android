package co.elastic.otel.android.integration

import android.app.Activity
import android.os.Bundle
import co.elastic.otel.android.extensions.log
import co.elastic.otel.android.extensions.span
import co.elastic.otel.android.integration.MyApp.Companion.agent

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val meter = agent.getOpenTelemetry().getMeter("co.elastic.android.integration")
        meter.counterBuilder("meter counter").build().add(5)

        agent.span("span name") {
            agent.log("log body")
        }
    }
}