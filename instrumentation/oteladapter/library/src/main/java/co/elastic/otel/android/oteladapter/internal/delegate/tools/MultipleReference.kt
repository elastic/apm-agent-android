package co.elastic.otel.android.oteladapter.internal.delegate.tools

import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentSet

class MultipleReference<T>(
    private val noopValue: T,
    private val delegatorFactory: (T) -> Delegator<T>
) {
    private val references = WeakConcurrentSet<Delegator<T>>(WeakConcurrentSet.Cleaner.INLINE)

    @Suppress("UNCHECKED_CAST")
    fun maybeAdd(value: T): T {
        if (value != noopValue) {
            val delegator = delegatorFactory.invoke(value)
            references.expungeStaleEntries()
            references.add(delegator)
            return delegator as T
        }

        return value
    }

    fun reset() {
        for (delegator in references) {
            delegator?.reset()
        }
        references.clear()
    }
}