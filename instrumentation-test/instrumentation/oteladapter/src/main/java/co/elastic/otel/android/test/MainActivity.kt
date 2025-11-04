package co.elastic.otel.android.test

import android.app.Activity
import android.util.Log

class MainActivity : Activity() {

    fun sendLog() {
        Log.d("elastic", "My log")
    }
}