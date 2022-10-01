package co.elastic.apm.android.test.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import co.elastic.apm.android.test.activities.espresso.IdlingResourceProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class StuckCoroutineActivity : AppCompatActivity(), IdlingResourceProvider {
    private val idling = CountingIdlingResource("coroutine-idling-resource")
    private val someFlow = flow {
        emit(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idling.increment()
        lifecycleScope.launch {
            someFlow.collectLatest {
                idling.decrement()
            }
        }
    }

    override fun getIdlingResource(): IdlingResource {
        return idling
    }
}