package com.luisli.cagpt.utils
import androidx.lifecycle.Observer
class EventObserver<T>(private val onEventUnconsumedContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>) {
        event.consume()?.run(onEventUnconsumedContent)
    }
}
