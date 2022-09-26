package co.elastic.apm.android.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.opentelemetry.context.Context

class MissingOnResumeActivity : AppCompatActivity() {
    var onCreateSpanContext: Context? = null
    var onStartSpanContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateSpanContext = Context.current()
    }

    override fun onStart() {
        super.onStart()
        onStartSpanContext = Context.current()
    }
}