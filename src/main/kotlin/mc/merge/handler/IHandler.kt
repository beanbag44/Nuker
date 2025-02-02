package mc.merge.handler

interface IHandler {
    var currentlyBeingUsedBy: IHandlerController?

    private fun setUsedBy(module: IHandlerController) {
        currentlyBeingUsedBy = module
    }

    private fun clearUsedBy() {
        currentlyBeingUsedBy = null
    }

//    fun getUsabilityState(): Enum<*>
}