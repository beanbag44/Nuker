package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event
import me.beanbag.nuker.eventsystem.events.ICancellable

object EventBus {
    val callbackHolderMap = hashMapOf<Class<out Event>, ArrayList<Pair<Class<*>, (Event) -> Unit>>>()

    inline fun <reified T: Event> subscribe(callbackHolder: Class<*>, noinline callback: (Event) -> Unit) {
        callbackHolderMap.getOrPut(T::class.java) { arrayListOf() }
            .add(Pair(callbackHolder, callback))
    }

    fun unsubscribe(clazz: Class<*>) {
        callbackHolderMap.values.forEach {
            it.removeIf { callbackPair ->
                callbackPair.first == clazz
            }
        }
    }

    fun post(event: Event) {
        callbackHolderMap[event::class.java]?.let { callbackList ->
            for (pair in callbackList) {
                pair.second.invoke(event)
                if (event is ICancellable && event.isCanceled()) break
            }
        }
    }
}