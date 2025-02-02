package mc.merge.event.events

interface ICancellable {
    var canceled: Boolean

    fun cancel() {
        canceled = true
    }

    fun isCanceled(): Boolean {
        return canceled
    }
}