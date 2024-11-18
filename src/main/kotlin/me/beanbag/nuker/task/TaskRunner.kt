package me.beanbag.nuker.task

//import me.beanbag.nuker.eventsystem.CallbackHolder
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.TickEvent

class TaskRunner {
    /**
     * Tasks that run in order added
     * */
    private val taskQueue: MutableList<Task> = mutableListOf()
    /**
     * Tasks that run asynchronously
     * */
    private val concurrentTasks: MutableList<Task> = mutableListOf()

    private var lastFinishedTask: Task? = null

    init {
        EventBus.subscribe<TickEvent.Pre>(this, 0, this::onTick)
    }

    fun addTask(task: Task) {
        EventBus.unsubscribe(task)
        taskQueue.add(task)
    }

    fun addConcurrentTask(task: Task) {
        concurrentTasks.add(task)
    }

    fun onTick(event: TickEvent.Pre) {
        if (taskQueue.isEmpty()) return

        val task = taskQueue.first()
        if (task.isFinished) {
            lastFinishedTask = taskQueue.removeAt(0)
        } else if (!task.isActive) {
            EventBus.resubscribe(task)
            task.run()
        }
        concurrentTasks.forEach { if (!it.isActive) it.run() }
        concurrentTasks.apply {
            removeIf{
                it.isFinished
            }
        }
    }

    fun isRunningTask(): Boolean {
        return taskQueue.isNotEmpty() || concurrentTasks.isNotEmpty()
    }

    fun stopAllTasks() {
        val activeTask = taskQueue.firstOrNull()
        taskQueue.clear()
        activeTask?.finish()
        concurrentTasks.forEach { it.finish() }
        concurrentTasks.clear()
    }
}