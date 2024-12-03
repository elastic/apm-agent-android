package co.elastic.apm.android.sdk.tools

internal class MultiInterceptor<T>(private val interceptors: List<Interceptor<T>>) :
    Interceptor<T> {

    override fun intercept(item: T): T {
        var result = item
        interceptors.forEach {
            result = it.intercept(result)
        }
        return result
    }
}