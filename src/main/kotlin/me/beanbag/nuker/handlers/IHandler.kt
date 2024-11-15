package me.beanbag.nuker.handlers

interface IHandler {
    var currentlyBeingUsedBy: Module?
    var priority: Int

    fun setUsedBy(module: Module) {
        currentlyBeingUsedBy = module
    }

    fun clearUsedBy() {
        currentlyBeingUsedBy = null
    }
}