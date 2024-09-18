package me.beanbag.nuker.eventsystem

import me.beanbag.nuker.eventsystem.events.Event
import me.beanbag.nuker.eventsystem.events.ICancellable
import kotlin.reflect.KClass

object EventBus {
    val callbackHolderMap = hashMapOf<KClass<out Event>, ArrayList<Pair<KClass<*>, (Event) -> Unit>>>()

    inline fun <reified T: Event> subscribe(callbackHolder: KClass<*>, noinline callback: (Event) -> Unit) {
        callbackHolderMap.getOrPut(T::class) { arrayListOf() }
            .add(Pair(callbackHolder, callback))
    }

    fun unsubscribe(clazz: KClass<*>) {
        callbackHolderMap.values.forEach {
            it.removeIf { callbackPair ->
                callbackPair.first == clazz
            }
        }
    }

    fun post(event: Event) {
        callbackHolderMap[event::class]?.let { callbackList ->
            for (pair in callbackList) {
                pair.second.invoke(event)
                if (event is ICancellable && event.isCanceled()) break
            }
        }
    }
}