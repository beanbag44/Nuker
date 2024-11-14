package me.beanbag.nuker.handlers

object HandlerHandler {
    private var sequenceCount = 0
    private var currentActiveHandlers = hashSetOf<IHandler>()

    val handlerList = sortedSetOf(
        compareBy { it.priority },
        BreakingHandler,
        BrokenBlockHandler,
        ChatHandler,
        PlacementHandler
    )

    fun setActiveHandler(handler: IHandler) {
        currentActiveHandlers.add(handler)
    }

    fun setInactiveHandler(handler: IHandler) {
        currentActiveHandlers.remove(handler)
    }

}