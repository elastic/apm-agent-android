package co.elastic.apm.android.test.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.opentelemetry.context.Context

class FullCreationActivity : AppCompatActivity() {
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
    }

    override fun onResume() {
        super.onResume()
        onResumeSpanContext = Context.current()
    }
}