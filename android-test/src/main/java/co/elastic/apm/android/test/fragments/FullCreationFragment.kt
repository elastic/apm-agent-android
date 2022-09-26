package co.elastic.apm.android.test.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.elastic.apm.android.test.R
import io.opentelemetry.context.Context

class FullCreationFragment : Fragment(R.layout.fragment_full_creation) {
    var onCreateSpanContext: Context? = null
    var onCreateViewSpanContext: Context? = null
    var onViewCreatedSpanContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateSpanContext = Context.current()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        onCreateViewSpanContext = Context.current()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedSpanContext = Context.current()
    }
}
