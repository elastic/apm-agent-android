package co.elastic.apm.android.test.okhttp

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource
import co.elastic.apm.android.sdk.traces.ElasticTracer
import co.elastic.apm.android.test.activities.espresso.IdlingResourceProvider
import okhttp3.*
import java.io.IOException

class NetworkCallingActivity : AppCompatActivity(),
    IdlingResourceProvider {
    private val idling = CountingIdlingResource("okhttp")

    fun makeNetworkCall(mockServerUrl: HttpUrl) {
        val client = OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request()

                it.proceed(request.newBuilder().addHeader("MY-HEADER", "My header value").build())
            }.build()

        val request = Request.Builder()
            .url(mockServerUrl)
            .build()

        val span = ElasticTracer.androidActivity().spanBuilder("Http parent span").startSpan()
        span.makeCurrent().use {
            idling.increment()
            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    idling.decrement()
                }

                override fun onResponse(call: Call, response: Response) {
                    idling.decrement()
                }
            })
        }
        span.end()
    }

    override fun getIdlingResource(): IdlingResource {
        return idling
    }
}