package me.beanbag.nuker.task

import baritone.api.pathing.goals.GoalBlock
import me.beanbag.nuker.ModConfigsJava.baritoneProcess
import me.beanbag.nuker.ModConfigsJava.mc
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.TickEvent
import net.minecraft.util.math.BlockPos

class BaritonePathTask(val pos: BlockPos) : Task() {
    override fun run() {
        super.run()
        baritoneProcess.pathToGoal(GoalBlock(pos))

        EventBus.subscribe<TickEvent.Pre>(this) {
            if (mc.player?.blockPos == pos) {
                baritoneProcess.pauseBaritone()
                finish()
            }
        }
    }

    override fun finish() {
        super.finish()
        baritoneProcess.releaseControl()
    }
}