package me.beanbag.nuker.types

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Supplier

class TimeoutSet<T>(private var timeout: Supplier<Int>) {
    private val map = ConcurrentHashMap<TickCounter, T>()
    private var onTimeout: Consumer<T>? = null

    init {
        EventBus.onEvent<TickEvent.Pre> {
            map.keys.forEach {
                it.tickCounter()
            }

            updateMap()
        }
    }

    fun put(value: T) {
        map[TickCounter()] = value
    }

    fun values() =
        map.values

    fun setOnTimeout(timeoutConsumer: Consumer<T>): TimeoutSet<T> {
        onTimeout = timeoutConsumer
        return this
    }

    private fun updateMap() {
        map.keys.removeIf { tickCounter ->
            if (tickCounter.counter >= timeout.get()) {
                map[tickCounter]?.let { onTimeout?.accept(it) }
                return@removeIf true
            }

            false
        }
    }
}