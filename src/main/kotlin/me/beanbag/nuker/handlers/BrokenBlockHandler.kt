package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.module.modules.CoreConfig.ghostBlockTimeout
import me.beanbag.nuker.module.modules.CoreConfig.validateBreak
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.BlockUtils.emulateBlockBreak
import me.beanbag.nuker.utils.BlockUtils.isBlockBroken
import me.beanbag.nuker.utils.InGame
import me.beanbag.nuker.utils.ThreadUtils
import net.minecraft.block.BlockState
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.util.math.BlockPos

object BrokenBlockHandler : IHandler {
    override var priority = 0
    override var currentlyBeingUsedBy: IHandlerController? = null

    private val blockQueue = TimeoutSet<BrokenBlockPos> { if (validateBreak) 40 else ghostBlockTimeout }
        .setOnTimeout { pos ->
            if (!pos.broken) return@setOnTimeout
            mc.world?.setBlockState(pos, pos.state)
        }

    init {
        onInGameEvent<PacketEvent.Receive.Pre> { event ->
            val packet = event.packet

            if (packet is BlockUpdateS2CPacket) {
                onBlockUpdate(packet.pos, packet.state)
            } else if (packet is ChunkDeltaUpdateS2CPacket) {
                packet.visitUpdates { pos, state ->
                    onBlockUpdate(pos, state)
                }
            }
        }
    }

    fun putBrokenBlock(pos: BlockPos, state: BlockState, broken: Boolean) {
        blockQueue.values().removeIf { it == pos }
        blockQueue.put(BrokenBlockPos(pos, state, broken))
    }

    private fun InGame.onBlockUpdate(pos: BlockPos, state: BlockState) {
        blockQueue.values().removeIf { queueBlockPos ->
            if (queueBlockPos != pos
                || !isBlockBroken(queueBlockPos.state, state)
                ) {
                return@removeIf false
            }

            if (!queueBlockPos.broken) {
                ThreadUtils.runOnMainThread {
                    emulateBlockBreak(queueBlockPos, queueBlockPos.state)
                }
            }

            return@removeIf true
        }
    }

    class BrokenBlockPos(
        blockPos: BlockPos,
        val state: BlockState,
        var broken: Boolean
    ) : BlockPos(blockPos.x, blockPos.y, blockPos.z)
}