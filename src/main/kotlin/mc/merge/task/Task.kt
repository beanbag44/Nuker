package mc.merge.task

import mc.merge.event.EventBus
import mc.merge.handler.HandlerPriority
import mc.merge.handler.IHandlerController

abstract class Task(val parent: IHandlerController) : IHandlerController {
    var isActive = false
    var isFinished = false

    var description: String = ""



    override fun getPriority(): HandlerPriority {
        return parent.getPriority()
    }

    open fun run() {
        isActive = true
    }

    open fun finish() {
        isActive = false
        isFinished = true
        EventBus.removeCallbacks(this)
    }
}