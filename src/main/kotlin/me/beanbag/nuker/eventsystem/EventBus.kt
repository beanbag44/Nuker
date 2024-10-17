package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event
import me.beanbag.nuker.eventsystem.events.ICancellable
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    private val eventToCallback =
        ConcurrentHashMap<Class<out Event>, ConcurrentHashMap.KeySetView<EventCallback, Boolean>>()
    private val unsubscribedEventToCallback =
        ConcurrentHashMap<Class<out Event>, ConcurrentHashMap.KeySetView<EventCallback, Boolean>>()

    inline fun <reified T : Event> subscribe(subscriber: Any, noinline callback: (T) -> Unit) =
        subscribe(subscriber, callback, T::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> subscribe(subscriber: Any, callback: (T) -> Unit, eventClass: Class<T>) =
        eventToCallback.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
            .add(EventCallback(callback as (Event) -> Unit, subscriber))


    fun resubscribe(subscriber: Any) = unsubscribedEventToCallback.forEach { (event, callbacks) ->
        callbacks.forEach { callback ->
            if (callback.subscriber == subscriber) {
                eventToCallback.getOrPut(event) { ConcurrentHashMap.newKeySet() }.add(callback)
            }
        }
    }

    fun unsubscribe(subscriber: Any) = eventToCallback.forEach { (event, callbacks) ->
        val unsubscribed = unsubscribedEventToCallback.getOrPut(event) { ConcurrentHashMap.newKeySet() }
        callbacks.forEach { callback ->
            if (callback.subscriber == subscriber) {
                unsubscribed.add(callback)
                callbacks.remove(callback)
            }
        }
    }


    fun removeCallbacks(subscriber: Any) {
        eventToCallback.values.forEach { callbacks ->
            callbacks.removeIf { it.subscriber == subscriber }
        }
        unsubscribedEventToCallback.values.forEach { callbacks ->
            callbacks.removeIf { it.subscriber == subscriber }
        }
    }

    fun post(event: Event) = eventToCallback[event::class.java]?.forEach { callback ->
        callback.callback.invoke(event)
        if (event is ICancellable && event.isCanceled()) return@post Unit
    }
}