package co.elastic.apm.android.test.attributes.logs

import io.opentelemetry.api.logs.GlobalLoggerProvider

class LogAttrHost {

    fun methodWithLog() {
        val logger = GlobalLoggerProvider.get().loggerBuilder("my-logger").build()
        val logRecordBuilder = logger.logRecordBuilder()
        logRecordBuilder.setBody("The log body").emit()
    }
}