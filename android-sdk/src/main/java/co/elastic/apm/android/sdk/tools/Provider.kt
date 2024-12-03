package co.elastic.apm.android.sdk.tools

interface Provider<T> {
    fun get(): T
}