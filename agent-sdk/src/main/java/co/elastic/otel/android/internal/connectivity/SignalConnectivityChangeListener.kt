package co.elastic.otel.android.internal.connectivity

import co.elastic.otel.android.internal.opentelemetry.SignalType

internal interface SignalConnectivityChangeListener {
    fun onConnectivityConfigurationChange(signalType: SignalType)
}