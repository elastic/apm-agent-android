package co.elastic.otel.android.test.oteladapter

import android.app.Activity
import android.util.Log

class MainActivity : Activity() {

    fun sendLog() {
        Log.d("elastic", "My log")
    }
}