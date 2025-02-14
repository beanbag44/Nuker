package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs

object HandlerHandler {
    private var sequenceCount = 0
    private var currentActiveHandlers = hashSetOf<IHandler>()

    fun setActiveHandler(handler: IHandler) {
        currentActiveHandlers.add(handler)
    }

    fun setInactiveHandler(handler: IHandler) {
        currentActiveHandlers.remove(handler)
    }

}