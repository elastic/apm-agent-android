package co.elastic.apm.android.test.activities

import androidx.appcompat.app.AppCompatActivity

class NoLifecycleMethodsActivity : AppCompatActivity() {

    override fun onPause() {
        super.onPause()
        // Not a "view loading" lifecycle method.
    }
}