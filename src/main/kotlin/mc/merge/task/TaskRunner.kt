package mc.merge.task

import mc.merge.event.EventBus
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent


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
        onInGameEvent<TickEvent.Pre> {
            if (taskQueue.isEmpty()) return@onInGameEvent

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
    }

    fun addTask(task: Task) {
        EventBus.unsubscribe(task)
        taskQueue.add(task)
    }

    fun addConcurrentTask(task: Task) {
        concurrentTasks.add(task)
        task.run()
    }

    fun isRunningTask(): Boolean {
        return taskQueue.isNotEmpty() || concurrentTasks.isNotEmpty()
    }

    fun stopAllTasks() {
        taskQueue.firstOrNull()?.finish()
        concurrentTasks.forEach { it.finish() }

        taskQueue.clear()
        concurrentTasks.clear()
    }

    fun stopTaskOfType(taskType: Class<out Task>) {
        taskQueue.filter { it::class.java == taskType }.forEach {
            if (it == taskQueue.firstOrNull()){
                it.finish()
            }
            taskQueue.remove(it)
        }
        concurrentTasks.filter { it::class.java == taskType }.forEach {
            it.finish()
            concurrentTasks.remove(it)
        }
    }
}