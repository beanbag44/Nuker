package mc.merge.handler

import mc.merge.event.events.PacketEvent
import mc.merge.event.onInGameEvent
import mc.merge.module.modules.CoreConfig.ghostBlockTimeout
import mc.merge.module.modules.CoreConfig.validateBreak
import mc.merge.types.TimeoutSet
import mc.merge.util.BlockUtils.emulateBlockBreak
import mc.merge.util.BlockUtils.isBlockBroken
import mc.merge.util.InGame
import mc.merge.util.ThreadUtils
import mc.merge.ModCore.mc
import net.minecraft.block.BlockState
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.util.math.BlockPos

object BrokenBlockHandler : IHandler {
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