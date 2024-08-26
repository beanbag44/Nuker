package me.beanbag.nuker.utils

import me.beanbag.nuker.types.TickCounter
import me.beanbag.nuker.types.TimeoutSet

object TimerUtils {
    private val tickTimerMaps = arrayListOf<HashMap<TickCounter, *>>()
    private val timeoutMaps = arrayListOf<TimeoutSet<*>>()

    fun tickTickTimerMaps() {
        tickTimerMaps.forEach {
            it.keys.forEach { counter -> counter.tickCounter() }
        }
    }

    fun updateTimeoutMaps() {
        timeoutMaps.forEach {
            it.updateMap()
        }
    }

    fun HashMap<TickCounter, *>.subscribeTickTimerMap() {
        tickTimerMaps.add(this)
    }

    fun TimeoutSet<*>.subscribeOnTickUpdate() {
        timeoutMaps.add(this)
    }
}