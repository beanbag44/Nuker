package me.beanbag.nuker.task

import me.beanbag.nuker.eventsystem.EventBus


abstract class Task {
    var isActive = false
    var isFinished = false

    var description: String = ""

    open fun run() {
        isActive = true
    }

    open fun finish() {
        isActive = false
        isFinished = true
        EventBus.removeCallbacks(this)
    }
}