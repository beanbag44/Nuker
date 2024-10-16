package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event

class CallbackHolder {
    private var subscribed = true

    val callbacks = hashMapOf<Class<out Event>, (any : Event) -> Unit>()

    inline fun <reified T: Event> addCallback(noinline callback: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        callbacks[T::class.java] = callback as (Event) -> Unit
    }

    fun<T: Event> addCallback(clazz: Class<out T>, callback: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        callbacks[clazz] = callback as (Event) -> Unit
    }

    fun unsubscribe() {
        subscribed = false
    }

    fun subscribe() {
        subscribed = true
    }

    fun isSubscribed() =
        subscribed
}