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
package co.elastic.apm.android.sdk.features.exportgate

import co.elastic.apm.android.sdk.internal.services.kotlin.backgroundwork.BackgroundWorkService
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class GateSpanExporter(
    capacity: Int,
    private val backgroundWorkService: BackgroundWorkService
) : SpanExporter {
    private val queue by lazy { LinkedBlockingQueue<SpanData>(capacity) }
    private val pendingLatches = AtomicInteger(0)
    private val open = AtomicBoolean(true)
    private val configurationFinished = AtomicBoolean(false)
    private var queuedInterceptor: Interceptor<SpanData> = Interceptor.noop()
    private lateinit var delegate: SpanExporter

    fun createLatch(): Latch {
        validateConfigurationFinished()
        open.compareAndSet(true, false)
        pendingLatches.incrementAndGet()
        return object : Latch {
            private val opened = AtomicBoolean(false)

            override fun open() {
                if (opened.compareAndSet(false, true)) {
                    val size = pendingLatches.decrementAndGet()
                    if (size == 0) {
                        open.set(true)
                        delegateQueued()
                    }
                }
            }
        }
    }

    fun setDelegate(value: SpanExporter) {
        validateConfigurationFinished()
        delegate = value
    }

    fun setQueuedDispatchingInterceptor(interceptor: Interceptor<SpanData>) {
        validateConfigurationFinished()
        queuedInterceptor = interceptor
    }

    private fun validateConfigurationFinished() {
        if (configurationFinished.get()) {
            throw IllegalStateException("The config process has already finished.")
        }
    }

    private fun delegateQueued() {
        if (queue.size == 0) {
            return
        }
        backgroundWorkService.submit {
            val items = mutableListOf<SpanData>()
            var item = queue.poll()
            while (item != null) {
                items.add(queuedInterceptor.intercept(item))
                item = queue.poll()
            }
            delegate.export(items)
        }
    }

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        configurationFinished.compareAndSet(false, true)
        return if (open.get()) {
            delegate.export(spans)
        } else {
            spans.forEach {
                queue.offer(it)
            }
            CompletableResultCode.ofSuccess()
        }
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }

    interface Latch {
        fun open()
    }
}
