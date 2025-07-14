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
package co.elastic.otel.android.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.any
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestListener
import java.io.Closeable
import java.time.Duration
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.rules.ExternalResource

class WireMockRule : ExternalResource() {
    private val wireMock = WireMockServer()
    private val requestManagers = LinkedList<RequestManager>()
    private var allResponsesStubId = UUID.randomUUID()

    override fun before() {
        wireMock.start()
    }

    override fun after() {
        var manager = requestManagers.poll()
        while (manager != null) {
            manager.close()
            manager = requestManagers.poll()
        }
        wireMock.stop()
    }

    fun url(path: String): String {
        return wireMock.url(path)
    }

    fun stubAllHttpResponses(
        responseVisitor: ResponseDefinitionBuilder.() -> Unit = {}
    ) {
        wireMock.removeStub(allResponsesStubId)
        val mappingBuilder = any(anyUrl()).withId(allResponsesStubId)
        val response = aResponse()
        responseVisitor(response)
        mappingBuilder.willReturn(response)
        wireMock.stubFor(mappingBuilder)
    }

    fun getRequestSize(): Int = wireMock.allServeEvents?.size ?: 0

    fun takeRequest(awaitSeconds: Int = 2): Request {
        await.atMost(Duration.ofSeconds(awaitSeconds.toLong()))
            .until { wireMock.allServeEvents?.isNotEmpty() }
        val allServeEvents = wireMock.allServeEvents
        assertThat(allServeEvents).hasSize(1)
        val serveEvent = allServeEvents?.first()
        wireMock.removeServeEvent(serveEvent?.id)
        return serveEvent!!.request
    }

    fun stubRequests(): RequestManager {
        val requestManager = RequestManager()
        requestManagers.add(requestManager)
        return requestManager
    }

    inner class RequestManager : Closeable {
        private val isClosed = AtomicBoolean(false)
        private val nextResponseReady = AtomicBoolean(false)
        private val uuid = UUID.randomUUID()
        private var requests: LinkedBlockingQueue<Request>? = LinkedBlockingQueue<Request>()
        private var responses: Queue<ResponseDefinitionBuilder>? = LinkedList()
        private var mappingBuilder: MappingBuilder? = any(anyUrl()).withId(uuid)
        private var listener: RequestListener? = RequestListener { request, _ ->
            if (isClosed.get()) {
                throw IllegalStateException("Request manager closed")
            }

            // Remove completed stub
            wireMock.removeStub(uuid)
            nextResponseReady.set(false)

            // Add next response
            tryEnqueueNextResponse()

            // Store request
            requests!!.add(request)
        }

        init {
            // Listen to all requests
            wireMock.addMockServiceRequestListener(listener)
        }

        fun enqueueResponse(responseVisitor: ResponseDefinitionBuilder.() -> Unit = {}) = apply {
            val response = aResponse()
            responseVisitor(response)
            responses!!.add(response)

            tryEnqueueNextResponse()
        }

        fun takeNextRequest(awaitSeconds: Int = 2): Request {
            return requests!!.poll(awaitSeconds.toLong(), TimeUnit.SECONDS)
        }

        private fun tryEnqueueNextResponse() {
            responses!!.poll()?.let { response ->
                if (nextResponseReady.compareAndSet(false, true)) {
                    mappingBuilder!!.willReturn(response)
                    wireMock.stubFor(mappingBuilder)
                }
            }
        }

        override fun close() {
            if (isClosed.compareAndSet(false, true)) {
                if (requests!!.isNotEmpty()) {
                    throw IllegalStateException("Not all requests were used")
                }
                wireMock.removeStub(uuid)
                mappingBuilder = null
                listener = null
                requests = null
                responses = null
            }
        }
    }
}