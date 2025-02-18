package mc.merge.util

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.Goal
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import mc.merge.ModCore

class BaritoneProcess : IBaritoneProcess {
    private var goal: Goal? = null
    private var commandType: PathingCommandType? = null
    private var isProcessActive: Boolean = false

    var isPaused: Boolean = false

    init {
        BaritoneAPI.getProvider().primaryBaritone.pathingControlManager.registerProcess(this)
    }

    override fun isActive(): Boolean {
        return isProcessActive
    }

    override fun onTick(b: Boolean, isSafeToCancel: Boolean): PathingCommand {
        if (isPaused) {
            return PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
        }
        if (goal != null && goal!!.isInGoal(ModCore.mc.player!!.blockPos) && isSafeToCancel) {
            return PathingCommand(null, PathingCommandType.DEFER)
        }

        if (commandType == null && goal != null) {
            commandType = PathingCommandType.SET_GOAL_AND_PATH
            return PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH)
        }
        if (commandType == null) {
            return PathingCommand(null, PathingCommandType.DEFER)
        }
        return PathingCommand(goal, commandType)
    }

    override fun isTemporary(): Boolean {
        return true
    }

    override fun onLostControl() {
        commandType = null
        goal = null
        isProcessActive = false
    }

    override fun displayName0(): String {
        return "Canal Tools Process"
    }


    fun pathToGoal(goal: Goal?) {
        this.goal = goal
        commandType = null
        isProcessActive = true
    }

    fun pauseBaritone() {
        isProcessActive = true
        isPaused = true
    }

    fun resumeBaritone() {
        isPaused = false
        if (commandType == null) {
            isProcessActive = false
        }
    }

    fun releaseControl() {
        onLostControl()
    }
}
