package co.elastic.apm.android.test

import android.app.Activity
import android.os.Bundle

class ErrorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw IllegalStateException("Creating exception")
    }
}