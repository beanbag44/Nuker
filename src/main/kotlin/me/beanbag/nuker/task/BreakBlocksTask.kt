package me.beanbag.nuker.task

import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.BreakingHandler.checkAttemptBreaks
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.BlockUtils
import me.beanbag.nuker.utils.BlockUtils.getBlockSphere
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

/**
 * Breaks all blocks in a given radius around the player with a filter passed in.
 */

class BreakBlocksTask(val shouldBreak: (pos: BlockPos, state: BlockState) -> Boolean, val mineStyle:VolumeSort) : Task() {

    override fun run() {
        super.run()
        onInGameEvent<TickEvent.Pre> {
            var blocksToBreak = getBlockSphere(player.eyePos, CoreConfig.radius) { pos, state -> !shouldBreak(pos, state) }
            blocksToBreak = BlockUtils.sortBlockVolume(ArrayList(blocksToBreak), player.eyePos, mineStyle)
            if (blocksToBreak.isEmpty()) {
                this@BreakBlocksTask.finish()
                return@onInGameEvent
            }

            checkAttemptBreaks(blocksToBreak)
        }
    }
}