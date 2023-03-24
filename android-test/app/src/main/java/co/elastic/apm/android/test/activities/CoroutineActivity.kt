package co.elastic.apm.android.test.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import co.elastic.apm.android.sdk.traces.ElasticTracers
import co.elastic.apm.android.test.activities.espresso.IdlingResourceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CoroutineActivity : AppCompatActivity(),
    IdlingResourceProvider {
    private val idling = CountingIdlingResource("coroutine-idling-resource")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idling.increment()
        lifecycleScope.launch {
            someSuspendFunction()
        }
    }

    private suspend fun someSuspendFunction() {
        val span =
            ElasticTracers.coroutine().spanBuilder("My span inside a coroutine").startSpan()

        delay(10)

        span.end()
        idling.decrement()
    }

    override fun getIdlingResource(): IdlingResource {
        return idling
    }
}