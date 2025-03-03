package mc.merge.task

import mc.merge.ModCore.breakingHandler
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.IHandlerController
import mc.merge.module.modules.CoreConfig
import mc.merge.types.VolumeSort
import mc.merge.util.BlockUtils
import mc.merge.util.BlockUtils.getBlockSphere
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos


/**
 * Breaks all blocks in a given radius around the player with a filter passed in.
 */

class BreakBlocksTask(
    val shouldBreak: (pos: BlockPos, state: BlockState) -> Boolean,
    val mineStyle: VolumeSort,
    parent: IHandlerController) : Task(parent) {

    override fun run() {
        super.run()
        onInGameEvent<TickEvent.Pre> {
            var blocksToBreak = getBlockSphere(player.eyePos, CoreConfig.breakRadius) { pos, state -> !shouldBreak(pos, state) }
            blocksToBreak = BlockUtils.sortBlockVolume(ArrayList(blocksToBreak), player.eyePos, mineStyle)
            if (blocksToBreak.isEmpty()) {
                this@BreakBlocksTask.finish()
                return@onInGameEvent
            }

            breakingHandler.breakBlocks(blocksToBreak, parent)
        }
    }
}