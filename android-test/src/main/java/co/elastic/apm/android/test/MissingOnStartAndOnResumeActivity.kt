package co.elastic.apm.android.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.opentelemetry.context.Context

class MissingOnStartAndOnResumeActivity : AppCompatActivity() {
    var onCreateSpanContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateSpanContext = Context.current()
    }
}