package mc.merge.handler

interface IHandlerController {
    fun getPriority(): HandlerPriority
}

class HandlerPriority(val priority: Int, val isLifeSupport:Boolean, val isExternal: Boolean = false) {

    companion object {
        fun lowest() : HandlerPriority = HandlerPriority(-100, false)
        fun normal() : HandlerPriority = HandlerPriority(0, false)
        fun highest() : HandlerPriority = HandlerPriority(100, false)
    }

    operator fun compareTo(other: HandlerPriority): Int {
        val chooseThis = 1
        val chooseOther = -1
        return if (isExternal != other.isExternal) {
            if (isExternal) chooseThis else chooseOther
        } else if (isLifeSupport != other.isLifeSupport) {
            if (isLifeSupport) chooseThis else chooseOther
        } else {
            priority.compareTo(other.priority)
        }
    }
}