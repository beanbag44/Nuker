package me.beanbag.nuker.inventory


interface IInventoryResult

class CantControl : IInventoryResult
class Error(val message: String) : IInventoryResult
class Interacted : IInventoryResult
class Started : IInventoryResult {
    private var onFinish: (() -> Unit)? = null
    fun awaitFinish(onFinish: () -> Unit) {
        this.onFinish = onFinish
    }
    fun finish() {
        onFinish?.invoke()
    }
}