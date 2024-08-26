package me.beanbag.nuker.types

import me.beanbag.nuker.utils.TimerUtils.subscribeTickTimerMap
import java.util.function.Consumer
import java.util.function.Supplier

class TimeoutSet<T>(private var timeout: Supplier<Int>) {
    private val map = hashMapOf<TickCounter, T>().apply { subscribeTickTimerMap() }
    private var onTimeout: Consumer<T>? = null

    fun put(value: T) {
        map.put(TickCounter(), value)
    }

    fun values() =
        map.values

    fun setOnTimeout(timeoutConsumer: Consumer<T>): TimeoutSet<T> {
        onTimeout = timeoutConsumer
        return this
    }

    fun updateMap() {
        map.keys.removeIf { tickCounter ->
            if (tickCounter.counter >= timeout.get()) {
                map[tickCounter]?.let { onTimeout?.accept(it) }
                return@removeIf true
            }

            false
        }
    }
}