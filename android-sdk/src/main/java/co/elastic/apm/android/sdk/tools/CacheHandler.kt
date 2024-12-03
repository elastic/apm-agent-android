package co.elastic.apm.android.sdk.tools

interface CacheHandler<T> {
    fun retrieve(): T?

    fun store(value: T)

    fun clear()
}