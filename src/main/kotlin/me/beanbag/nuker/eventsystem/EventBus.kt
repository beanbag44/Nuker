package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event
import me.beanbag.nuker.eventsystem.events.ICancellable
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    private val callbackHolders = ConcurrentHashMap.newKeySet<CallbackHolder>()

    fun addCallbackHolder(callbackHolder: CallbackHolder) {
        callbackHolders.add(callbackHolder)
    }

    fun post(event: Event): Event {
        for (callbackHolder in callbackHolders) {
            if (!callbackHolder.isSubscribed()) continue
            for (callback in callbackHolder.callbacks) {
                if (!callback.key.isInstance(event)) continue
                callback.value.invoke(event)
                if (event is ICancellable && event.isCanceled()) break
            }
        }
        return event
    }
}