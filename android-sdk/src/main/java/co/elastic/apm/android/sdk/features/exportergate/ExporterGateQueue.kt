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
package co.elastic.apm.android.sdk.features.exportergate

import co.elastic.apm.android.sdk.features.exportergate.latch.Latch
import co.elastic.apm.android.sdk.tools.interceptor.Interceptor
import io.opentelemetry.sdk.common.CompletableResultCode
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class ExporterGateQueue<DATA>(
    capacity: Int,
    private val listener: Listener,
    private val id: Int
) {
    private val queue by lazy { LinkedBlockingQueue<DATA>(capacity) }
    private val overflow by lazy { mutableListOf<DATA>() }
    private val pendingLatches = AtomicInteger(0)
    private val open = AtomicBoolean(true)
    private val started = AtomicBoolean(false)
    private var queuedInterceptor: Interceptor<DATA> = Interceptor.noop()

    fun createLatch(): Latch {
        open.compareAndSet(true, false)
        pendingLatches.incrementAndGet()
        return object : Latch {
            private val opened = AtomicBoolean(false)

            override fun open() {
                if (opened.compareAndSet(false, true)) {
                    val size = pendingLatches.decrementAndGet()
                    if (size == 0) {
                        openGate()
                    }
                }
            }
        }
    }

    fun setQueueProcessingInterceptor(interceptor: Interceptor<DATA>) {
        queuedInterceptor = interceptor
    }

    fun enqueue(data: Collection<DATA>): CompletableResultCode {
        if (started.compareAndSet(false, true)) {
            listener.onStartEnqueuing(id)
        }
        var surpassedQueueSize = false
        for (item in data) {
            if (!queue.offer(item)) {
                surpassedQueueSize = true
                overflow.add(item)
            }
        }
        if (surpassedQueueSize) {
            openGate()
        }
        return CompletableResultCode.ofSuccess()
    }

    internal fun hasAvailableItems(): Boolean {
        return queue.size > 0 || overflow.isNotEmpty()
    }

    internal fun getProcessedItems(): Collection<DATA> {
        val items = mutableListOf<DATA>()
        var item = queue.poll()
        while (item != null) {
            items.add(queuedInterceptor.intercept(item))
            item = queue.poll()
        }
        items.addAll(overflow)
        overflow.clear()
        return items
    }

    internal fun openGate() {
        if (open.compareAndSet(false, true)) {
            listener.onOpen(id)
        }
    }

    interface Listener {
        fun onOpen(id: Int)
        fun onStartEnqueuing(id: Int)
    }
}