package co.elastic.apm.android.test.attributes.logs

import co.elastic.apm.android.sdk.logs.ElasticEvents
import co.elastic.apm.android.sdk.logs.ElasticLoggers
import io.opentelemetry.api.common.Attributes

class LogAttrHost {

    fun methodWithLog() {
        val logger = ElasticLoggers.builder("my-logger").build()
        val logRecordBuilder = logger.logRecordBuilder()
        logRecordBuilder.setBody("The log body").emit()
    }

    fun methodWithEvent() {
        val eventEmitter = ElasticEvents.builder("my-event-logger")
            .build()
        eventEmitter.emit("My event name", Attributes.empty());
    }
}