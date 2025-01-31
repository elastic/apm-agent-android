package co.elastic.otel.android.test

import android.app.Application

class InstrumentationApplication : Application() {
    lateinit var callback: Callback

    override fun onCreate() {
        super.onCreate()
        callback.onCreate(this)
    }

    interface Callback {
        fun onCreate(application: Application)
    }
}