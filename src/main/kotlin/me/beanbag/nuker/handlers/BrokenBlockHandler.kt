package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.CallbackHolder
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.module.modules.CoreConfig.ghostBlockTimeout
import me.beanbag.nuker.module.modules.CoreConfig.validateBreak
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.BlockUtils.isBlockBroken
import me.beanbag.nuker.utils.BlockUtils.state
import me.beanbag.nuker.utils.TimerUtils.subscribeOnTickUpdate
import net.minecraft.block.BlockState
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.util.math.BlockPos

object BrokenBlockHandler {
    private val blockQueue = TimeoutSet<BrokenBlockPos> { if (validateBreak) 40 else ghostBlockTimeout }
        .setOnTimeout { pos ->
        if (!pos.broken) return@setOnTimeout
        pos.previousState?.let { prevState ->
            mc.world?.setBlockState(pos, prevState)
        }
    }.apply { subscribeOnTickUpdate() }

    private val callbackHolder = CallbackHolder()

    init {
        EventBus.addCallbackHolder(callbackHolder)

        callbackHolder.addCallback<PacketEvent.Receive.Pre> { event ->
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

    class BrokenBlockPos(blockPos: BlockPos, var broken: Boolean) : BlockPos(blockPos.x, blockPos.y, blockPos.z) {
        val previousState = if (!validateBreak) blockPos.state else null
    }
}