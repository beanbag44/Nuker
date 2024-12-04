package me.beanbag.nuker.handlers

interface IHandler {
    var currentlyBeingUsedBy: Module?
    var priority: Int

    private fun setUsedBy(module: Module) {
        currentlyBeingUsedBy = module
    }

    private fun clearUsedBy() {
        currentlyBeingUsedBy = null
    }

//    fun getUsabilityState(): Enum<*>
}