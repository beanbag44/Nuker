package mc.merge.event

import mc.merge.event.EventBus.subscribe
import mc.merge.event.events.Event
import mc.merge.event.events.ICancellable
import mc.merge.util.InGame
import mc.merge.util.runInGame
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object EventBus {
    const val MIN_PRIORITY = -100
    const val MAX_PRIORITY = 100

    /** Map of event type to callbacks, sorted with the highest priority at the start and the lowest at the end */
    private val eventToCallback = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventCallback>>()
    private val unsubscribedEventToCallback = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventCallback>>()

    inline fun <reified T : Event> subscribe(subscriber: Any, priority: Int = 0, noinline callback: (T) -> Unit) =
        subscribe(subscriber, callback, T::class.java, priority)

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> subscribe(subscriber: Any, callback: (T) -> Unit, eventClass: Class<T>, priority: Int = 0) =
        addCallback(eventClass, EventCallback(callback as (Event) -> Unit, subscriber, priority))

    private fun addCallback(eventClass: Class<out Event>, callback: EventCallback) {
        val callbacks = eventToCallback.getOrPut(eventClass) { CopyOnWriteArrayList() }
        if (callbacks.isEmpty()) {
            callbacks.add(callback)
            return
        }

        var minIndex = 0
        var maxIndex = callbacks.size - 1
        if (callbacks[minIndex].priority < callback.priority) {
            callbacks.add(0, callback)
            return
        } else if (callbacks[maxIndex].priority > callback.priority) {
            callbacks.add(callback)
            return
        }
        //O(log n) approach for inserting a callback based on priority.
        //Inserts largest priority at the front of the list and smallest at the end.
        while (maxIndex - minIndex > 1) {
            val index = (maxIndex + minIndex) / 2
            if (callbacks[index].priority == callback.priority) {
                callbacks.add(index, callback)
                return
            } else if (callbacks[index].priority > callback.priority) {
                minIndex = index
            } else {
                maxIndex = index
            }
        }

        callbacks.add(maxIndex, callback)
    }

    fun resubscribe(subscriber: Any) = unsubscribedEventToCallback.forEach { (event, callbacks) ->
        callbacks.forEach { callback ->
            if (callback.subscriber == subscriber) addCallback(event, callback)
        }
        callbacks.removeIf{ it.subscriber == subscriber }
    }

    fun unsubscribe(subscriber: Any) = eventToCallback.forEach { (event, callbacks) ->
        callbacks.forEach { callback ->
            if (callback.subscriber == subscriber) {
                unsubscribedEventToCallback.getOrPut(event) { CopyOnWriteArrayList() }.add(callback)
            }
        }
        callbacks.removeIf { callback -> callback.subscriber == subscriber }
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


inline fun <reified T : Event> Any.onInGameEvent(priority: Int = 0, noinline callback: InGame.(T) -> Unit) =
    subscribe(this, { event: T -> runInGame { callback(event) } }, T::class.java, priority)


inline fun <reified T : Event> Any.onEvent(priority: Int = 0, noinline callback: (T) -> Unit) =
    subscribe(this, callback, T::class.java, priority)