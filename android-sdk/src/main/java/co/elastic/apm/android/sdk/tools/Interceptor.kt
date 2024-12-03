package co.elastic.apm.android.sdk.tools

interface Interceptor<T> {

    companion object {
        @JvmStatic
        fun <T> composite(interceptors: List<Interceptor<T>>): Interceptor<T> {
            if (interceptors.isEmpty()) {
                return noop()
            }

            if (interceptors.size == 1) {
                return interceptors.first()
            }

            return MultiInterceptor(interceptors)
        }

        @JvmStatic
        fun <T> noop(): Interceptor<T> {
            return NoopInterceptor()
        }
    }

    fun intercept(item: T): T
}