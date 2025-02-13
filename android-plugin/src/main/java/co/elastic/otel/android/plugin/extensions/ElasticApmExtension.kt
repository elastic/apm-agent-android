package co.elastic.otel.android.plugin.extensions

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

abstract class ElasticApmExtension @Inject constructor(objects: ObjectFactory) {
    val bytecodeInstrumentation = objects.newInstance(BytecodeInstrumentation::class.java)

    fun bytecodeInstrumentation(action: Action<BytecodeInstrumentation>) {
        action.execute(bytecodeInstrumentation)
    }
}