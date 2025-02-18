package mc.merge.task

import baritone.api.pathing.goals.GoalBlock
import mc.merge.ModCore.baritoneProcess
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.IHandlerController
import net.minecraft.util.math.BlockPos

class BaritonePathTask(val pos: BlockPos, parent: IHandlerController) : Task(parent) {
    override fun run() {
        super.run()
        baritoneProcess.pathToGoal(GoalBlock(pos))

        onInGameEvent<TickEvent.Pre> {
            if (mc.player?.blockPos == pos) {
                finish()
            }
        }
    }

    override fun finish() {
        super.finish()
        baritoneProcess.releaseControl()
    }
}