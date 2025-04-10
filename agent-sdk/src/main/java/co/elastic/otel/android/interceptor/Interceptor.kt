/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.interceptor

import co.elastic.otel.android.internal.utilities.interceptor.MultiInterceptor
import co.elastic.otel.android.internal.utilities.interceptor.NoopInterceptor

/**
 * Generic interface that allows to intercept an object.
 */
fun interface Interceptor<T> {

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

    /**
     * Intercepts an object of type [T].
     *
     * @param item The intercepted object.
     *
     * @return An object of type [T], it doesn't have to be the one received as parameter.
     */
    fun intercept(item: T): T
}