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
package co.elastic.otel.android.okhttp.internal.delegate

import java.util.concurrent.atomic.AtomicReference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
class InterceptorDelegator private constructor(private val initialValue: Interceptor) :
    Interceptor {
    private val delegate: AtomicReference<Interceptor> = AtomicReference(initialValue)

    override fun intercept(chain: Interceptor.Chain): Response {
        return delegate.get().intercept(chain)
    }

    fun setDelegate(value: Interceptor) {
        delegate.set(value)
    }

    fun reset() {
        setDelegate(initialValue)
    }

    companion object {
        private val NOOP_INTERCEPTOR = Interceptor { chain -> chain.proceed(chain.request()) }

        @JvmStatic
        fun create(): InterceptorDelegator {
            return InterceptorDelegator(NOOP_INTERCEPTOR)
        }
    }
}