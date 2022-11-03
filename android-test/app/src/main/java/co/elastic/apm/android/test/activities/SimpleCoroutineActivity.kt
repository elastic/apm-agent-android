package co.elastic.apm.android.test.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.elastic.apm.android.sdk.traces.common.tools.ElasticTracer
import kotlinx.coroutines.launch

class SimpleCoroutineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val span =
                ElasticTracer.androidActivity().spanBuilder("My Span Inside Coroutine").startSpan()

            Log.d("Elastic", "Search")

            span.end()
        }
    }
}