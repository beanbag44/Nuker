package me.beanbag.nuker.utils

import me.beanbag.nuker.types.TickCounter

object TimerUtils {
    private val tickTimerMaps = arrayListOf<HashMap<TickCounter, *>>()

    fun tickTickTimerMaps() =
        tickTimerMaps.forEach {
            it.keys.forEach { counter -> counter.tickCounter() }
        }

    fun HashMap<TickCounter, *>.subscribeTickTimerMap(): HashMap<TickCounter, *> {
        tickTimerMaps.add(this)
        return this
    }
}