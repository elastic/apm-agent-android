package co.elastic.apm.android.sdk.tools

internal class NoopInterceptor<T> : Interceptor<T> {

    override fun intercept(item: T): T {
        return item
    }
}