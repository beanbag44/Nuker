package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.modules.Nuker.ghostBlockTimeout
import me.beanbag.nuker.modules.Nuker.validateBreak
import me.beanbag.nuker.types.BrokenBlockPos
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.BlockUtils.isBlockBroken
import me.beanbag.nuker.utils.BlockUtils.state
import me.beanbag.nuker.utils.TimerUtils.subscribeOnTickUpdate
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

object BrokenBlockHandler {
    private val blockQueue = TimeoutSet<BrokenBlockPos> { if (validateBreak) 40 else ghostBlockTimeout }
        .setOnTimeout { pos ->
        if (!pos.broken) return@setOnTimeout
        pos.previousState?.let { prevState ->
            mc.world?.setBlockState(pos, prevState)
        }
    }.apply { subscribeOnTickUpdate() }

    fun putBrokenBlock(pos: BlockPos, broken: Boolean) {
        blockQueue.put(BrokenBlockPos(pos, broken))
    }

    fun onBlockUpdate(pos: BlockPos, state: BlockState) {
        blockQueue.values().removeIf { queueBlockPos ->
            if (queueBlockPos != pos) return@removeIf false

            if (!queueBlockPos.broken) {
                if (!isBlockBroken(queueBlockPos.state, state)) return@removeIf false
                mc.interactionManager?.breakBlock(pos)
            }

            return@removeIf true
        }
    }
}