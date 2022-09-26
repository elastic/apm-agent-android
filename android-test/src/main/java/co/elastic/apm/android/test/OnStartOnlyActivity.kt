package co.elastic.apm.android.test

import androidx.appcompat.app.AppCompatActivity
import io.opentelemetry.context.Context

class OnStartOnlyActivity : AppCompatActivity() {
    var onStartSpanContext: Context? = null

    override fun onStart() {
        super.onStart()
        onStartSpanContext = Context.current()
    }
}