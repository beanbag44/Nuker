package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.modules.Nuker.ghostBlockTimeout
import me.beanbag.nuker.modules.Nuker.validateBreak
import me.beanbag.nuker.types.BrokenBlockPos
import me.beanbag.nuker.utils.BlockUtils.isBlockBroken
import me.beanbag.nuker.utils.BlockUtils.state
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

object BrokenBlockHandler {
    private val blockQueue = hashMapOf<Long, BrokenBlockPos>()

    fun onBreakingComplete(pos: BlockPos, broken: Boolean) {
        blockQueue[System.currentTimeMillis()] = BrokenBlockPos(pos, broken)
    }

    fun onBlockUpdate(pos: BlockPos, state: BlockState) {
        val iterator = blockQueue.values.iterator()
        while (iterator.hasNext()) {
            val queuedPos = iterator.next()

            if (queuedPos != pos) continue

            if (!queuedPos.broken) {
                if (!isBlockBroken(queuedPos.state, state)) return
                mc.interactionManager?.breakBlock(pos)
            }

            iterator.remove()
            return
        }
    }

    fun updateBlockQueue() {
        blockQueue.keys.removeIf { timeAdded ->
            val timeout = if (validateBreak) 2000 else ghostBlockTimeout

            if (System.currentTimeMillis() - timeAdded < timeout) return@removeIf false

            blockQueue[timeAdded]?.let { pos ->
                if (!pos.broken) return@let

                pos.previousState?.let { prevState ->
                    mc.world?.setBlockState(pos, prevState)
                }
            }

            true
        }
    }
}