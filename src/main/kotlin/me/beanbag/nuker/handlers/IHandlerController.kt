package me.beanbag.nuker.handlers

interface IHandlerController {
    fun getPriority(): HandlerPriority
}

class HandlerPriority(val priority: Int, val isLifeSupport:Boolean) {

    companion object {
        fun lowest() : HandlerPriority = HandlerPriority(-100, false)
        fun normal() : HandlerPriority = HandlerPriority(0, false)
        fun highest() : HandlerPriority = HandlerPriority(100, false)
    }

    operator fun compareTo(other: HandlerPriority): Int {
        return if (isLifeSupport == other.isLifeSupport) {
            priority.compareTo(other.priority)
        } else {
            if (isLifeSupport) -1 else 1
        }
    }
}