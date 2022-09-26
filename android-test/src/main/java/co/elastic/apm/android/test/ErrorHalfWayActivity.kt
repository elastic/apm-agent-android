package co.elastic.apm.android.test

import android.app.Activity
import android.os.Bundle
import io.opentelemetry.context.Context

class ErrorHalfWayActivity : Activity() {
    var onCreateSpanContext: Context? = null
    var onStartSpanContext: Context? = null
    var onResumeSpanContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateSpanContext = Context.current()
    }

    override fun onStart() {
        super.onStart()
        onStartSpanContext = Context.current()
        throw IllegalStateException("Creating exception in onStart")
    }

    override fun onResume() {
        super.onResume()
        onResumeSpanContext = Context.current()
    }
}