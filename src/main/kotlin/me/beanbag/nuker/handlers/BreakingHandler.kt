package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.modules.Nuker.blockTimeout
import me.beanbag.nuker.modules.Nuker.breakThreshold
import me.beanbag.nuker.modules.Nuker.doubleBreak
import me.beanbag.nuker.modules.Nuker.packetLimit
import me.beanbag.nuker.modules.Nuker.radius
import me.beanbag.nuker.modules.Nuker.validateBreak
import me.beanbag.nuker.types.BreakingContext
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.BlockUtils.state
import me.beanbag.nuker.utils.DoubleBreakUtils.shiftPrimaryDown
import me.beanbag.nuker.utils.InventoryUtils.calcBreakDelta
import me.beanbag.nuker.utils.InventoryUtils.getBestTool
import me.beanbag.nuker.utils.InventoryUtils.swapTo
import me.beanbag.nuker.utils.TimerUtils.subscribeOnTickUpdate
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object BreakingHandler {
    val blockTimeouts = TimeoutSet<BlockPos> { blockTimeout }.apply { subscribeOnTickUpdate() }
    private var breakingContexts = arrayOfNulls<BreakingContext>(2)
    private var packetCounter = 0

    fun checkAttemptBreaks(blockVolume: List<PosAndState>) {
        packetCounter = 0
        updateSelectedSlot()

        blockVolume.forEach { block ->
            val blockPos = block.blockPos

            if (isAtMaximumCurrentBreakingContexts()) return

            val bestTool = getBestTool(block.blockState, blockPos)

            firstOrNullContext()?.run {
                if (this.bestTool != bestTool) return@forEach
            }

            var requiredShift = false

            breakingContexts[0]?.run {
                breakingContexts.shiftPrimaryDown()
                requiredShift = true
            }

            val breakDelta = calcBreakDelta(block.blockState, blockPos, bestTool)

            val breakPacketCount = if (breakDelta >= 1) 1 else 3

            packetCounter += breakPacketCount

            if (packetCounter > packetLimit) return

            breakingContexts[0] = BreakingContext(
                blockPos,
                block.blockState,
                breakDelta,
                bestTool,
                requiredShift
            )

            if (breakingContexts[1] == null) {
                if (swapTo(bestTool)) packetCounter++
            }

            if (breakPacketCount == 1) {
                startBreakPacket(blockPos)
            } else {
                startPacketBreaking(blockPos)
            }

            if (breakDelta >= breakThreshold) {
                onBlockBreak(0)
            }

            blockTimeouts.put(blockPos)
        }
    }

    private fun onBlockBreak(contextIndex: Int) {
        breakingContexts[contextIndex]?.apply {
            if (breakType.isPrimary()) {
                stopBreakPacket(pos)
            }

            BrokenBlockHandler.putBrokenBlock(pos, !validateBreak)

            if (!validateBreak) {
                mc.interactionManager?.breakBlock(pos)
            }
        }
        nullifyBreakingContext(contextIndex)
    }

    private fun startPacketBreaking(pos: BlockPos) {
        startBreakPacket(pos)
        abortBreakPacket(pos)
        stopBreakPacket(pos)
    }

    private fun startBreakPacket(pos: BlockPos) =
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
        )

    private fun abortBreakPacket(pos: BlockPos) =
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP)
        )

    private fun stopBreakPacket(pos: BlockPos) =
        mc.networkHandler?.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)
        )

    private fun isAtMaximumCurrentBreakingContexts(): Boolean {
        if (doubleBreak) {
            if (breakingContexts.all { it != null }) {
                return true
            }
            breakingContexts[0]?.run {
                if (startedWithDoubleBreak) {
                    return true
                }
            }
        } else {
            if (breakingContexts.firstOrNull() != null) return false
        }

        return false
    }

    private fun firstOrNullContext(): BreakingContext? {
        breakingContexts[0]?.run { return this }
        breakingContexts[1]?.run { return this }
        return null
    }

    fun updateBreakingContexts() {
        breakingContexts.forEach {
            it?.apply {
                val index = breakingContexts.indexOf(this)

                mc.player?.let { player ->
                    if (player.eyePos.distanceTo(pos.toCenterPos()) > radius) {
                        nullifyBreakingContext(index)
                        return@forEach
                    }
                } ?: run {
                    nullifyBreakingContext(index)
                    return@forEach
                }

                if (pos.state != state) {
                    nullifyBreakingContext(index)
                    return@forEach
                }
                mineTicks++
                bestTool = getBestTool(state, pos)
                updateBreakDeltas(calcBreakDelta(state, pos, bestTool))

                val threshold = if (breakType.isPrimary()) {
                    breakThreshold
                } else {
                    1f
                }

                if (miningProgress > threshold) {
                    onBlockBreak(breakingContexts.indexOf(this))
                }
            }
        }
    }

    private fun nullifyBreakingContext(contextIndex: Int) {
        breakingContexts[contextIndex] = null
    }

    private fun updateSelectedSlot() =
        breakingContexts.firstOrNull()?.run {
            if (swapTo(bestTool)) packetCounter++
        }
}