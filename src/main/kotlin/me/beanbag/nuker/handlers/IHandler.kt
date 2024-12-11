package me.beanbag.nuker.handlers

interface IHandler {
    var currentlyBeingUsedBy: IHandlerController?
    var priority: Int

    private fun setUsedBy(module: IHandlerController) {
        currentlyBeingUsedBy = module
    }

    private fun clearUsedBy() {
        currentlyBeingUsedBy = null
    }

//    fun getUsabilityState(): Enum<*>
}