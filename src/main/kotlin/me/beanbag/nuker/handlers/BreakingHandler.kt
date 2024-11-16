package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.inventoryHandler
import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.module.modules.nuker.enumsettings.*
import me.beanbag.nuker.render.Renderer
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.TimeoutSet
import me.beanbag.nuker.utils.*
import me.beanbag.nuker.utils.BlockUtils.breakBlockWithRestrictionChecks
import me.beanbag.nuker.utils.BlockUtils.canReach
import me.beanbag.nuker.utils.BlockUtils.emulateBlockBreak
import me.beanbag.nuker.utils.BlockUtils.isBlockBroken
import me.beanbag.nuker.utils.BlockUtils.state
import me.beanbag.nuker.utils.InventoryUtils.percentDamagePerTick
import me.beanbag.nuker.utils.InventoryUtils.getBestTool
import me.beanbag.nuker.utils.InventoryUtils.swapTo
import net.minecraft.block.BlockState
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.awt.Color

object BreakingHandler : IHandler {
    override var currentlyBeingUsedBy: Module? = null
    override var priority: Int = 0

    val blockTimeouts = TimeoutSet<BlockPos> { CoreConfig.blockTimeout }
    var breakingContexts = arrayOfNulls<BreakingContext>(2)
    private var packetCounter = 0

    init {
        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY) {
            if (inventoryHandler.externalInControl()) {
                nullifyBreakingContext(0)
                nullifyBreakingContext(1)
                return@onInGameEvent
            }
            packetCounter = 0
            updateSelectedSlot()
            updateBreakingContexts()
        }

        onEvent<PacketEvent.Receive.Pre>{ event ->
            if (event.packet is InventoryS2CPacket) {
                println("InventoryS2CPacket")
                print(event.packet)
            }

            val packet = event.packet

            if (packet is BlockUpdateS2CPacket) {
                onBlockUpdate(packet.pos, packet.state)
            } else if (packet is ChunkDeltaUpdateS2CPacket) {
                packet.visitUpdates { pos, state ->
                    onBlockUpdate(pos, state)
                }
            }
        }

        onEvent<RenderEvent> { event ->
            breakingContexts.forEach { ctx ->
                ctx?.drawRenders(event.renderer)
            }
        }
    }

    fun InGame.checkAttemptBreaks(blockVolume: List<PosAndState>): List<PosAndState> {
        if (inventoryHandler.externalInControl()) return emptyList()
        val startedBlocks = mutableListOf<PosAndState>()
        blockVolume.forEach { block ->
            val primaryBreakingContext = breakingContexts[0]

            if (isAtMaximumCurrentBreakingContexts()) return startedBlocks

            val blockPos = block.blockPos

            if (breakingContexts.any { it?.pos == blockPos }) return@forEach

            val bestTool = getBestTool(block.blockState, blockPos)

            primaryBreakingContext?.run {
                if (this.bestTool != bestTool) return@forEach
            }

            val breakDelta = percentDamagePerTick(block.blockState, blockPos, bestTool)
            val isInstaBreak = breakDelta >= 1
            val breakPacketCount = if (isInstaBreak) 1 else 4

            packetCounter += breakPacketCount

            if (packetCounter > CoreConfig.packetLimit) return startedBlocks

            if (primaryBreakingContext != null) {
                breakingContexts.shiftPrimaryDown()
            }

            breakingContexts[0] = BreakingContext(
                blockPos,
                block.blockState,
                breakDelta,
                bestTool,
            ).apply {
                mineTicks++
            }

            if (breakingContexts[1] == null) {
                if (swapTo(bestTool)) packetCounter++
            }

            if (isInstaBreak) {
                startedBlocks.add(block)
                startBreakPacket(blockPos)
            } else {
                startedBlocks.add(block)
                startPacketBreaking(blockPos)
            }

            if (breakDelta >= CoreConfig.breakThreshold) {
                onBlockBreak(0)
            }
        }
        return startedBlocks
    }

    private fun InGame.onBlockBreak(contextIndex: Int) {
        breakingContexts[contextIndex]?.apply {
            BrokenBlockHandler.putBrokenBlock(pos, state, !CoreConfig.validateBreak)
            blockTimeouts.put(pos)

            if (!CoreConfig.validateBreak) {
                ThreadUtils.runOnMainThread {
                    breakBlockWithRestrictionChecks(pos)
                }
            }
        }
        nullifyBreakingContext(contextIndex)
    }

    private fun InGame.startPacketBreaking(pos: BlockPos) {
        abortBreakPacket(pos)
        stopBreakPacket(pos)
        startBreakPacket(pos)
        stopBreakPacket(pos)
    }

    private fun InGame.startBreakPacket(pos: BlockPos) {
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    private fun InGame.abortBreakPacket(pos: BlockPos) {
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    private fun InGame.stopBreakPacket(pos: BlockPos) {
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    private fun isAtMaximumCurrentBreakingContexts(): Boolean {
        return if (CoreConfig.doubleBreak)
            breakingContexts[1] != null
        else
            breakingContexts[0] != null
    }

    private fun firstOrNullContext(): BreakingContext? {
        breakingContexts[0]?.run { return this }
        breakingContexts[1]?.run { return this }
        return null
    }

    private fun InGame.updateBreakingContexts() {
        breakingContexts.forEach { it?.apply {
            val index = if (breakType.isPrimary()) 0 else 1

            if (!canReach(player.eyePos, PosAndState.from(pos, world), CoreConfig.radius) && breakType.isPrimary()) {
                abortBreakPacket(pos)
                nullifyBreakingContext(index)
                return@forEach
            }

            if (pos.state != state) {
                nullifyBreakingContext(index)
                return@forEach
            }
            mineTicks++
            bestTool = getBestTool(state, pos)
            updateBreakDeltas(percentDamagePerTick(state, pos, bestTool))

            val threshold = if (index == 0) {
                CoreConfig.breakThreshold
            } else {
                1.0f
            }

            if (miningProgress > threshold) {
                if (breakType.isPrimary()) {
                    stopBreakPacket(pos)
                    packetCounter++
                }
                packetCounter++
                onBlockBreak(index)
            }
        }}
    }

    private fun onBlockUpdate(pos: BlockPos, state: BlockState) {
        breakingContexts.forEach { ctx ->
            if (ctx == null) return@forEach
            if (ctx.pos != pos || !isBlockBroken(ctx.state, state)) return@forEach
            ThreadUtils.runOnMainThread {
                runInGame {
                    emulateBlockBreak(pos, ctx.state)
                }
            }
            nullifyBreakingContext(if (ctx.breakType.isPrimary()) 0 else 1)
        }
    }

    private fun Array<BreakingContext?>.shiftPrimaryDown() {
        if (this[0] == null) {
            this[1] = null
            return
        }
        this[0]?.breakType = BreakType.Secondary
        this[1] = this[0]
        this[0] = null
    }

    fun nullifyBreakingContext(contextIndex: Int) {
        breakingContexts[contextIndex] = null
    }

    private fun InGame.updateSelectedSlot() {
        breakingContexts.firstOrNull()?.run {
            if (swapTo(bestTool)) packetCounter++
        }
    }

    class BreakingContext(
        val pos: BlockPos,
        val state: BlockState,
        var currentBreakDelta: Float,
        var bestTool: Int,
    ) {
        var mineTicks: Int = 0
        var breakType = BreakType.Primary
        var additiveBreakDelta = currentBreakDelta
        var previousBreakDelta = 0f
        var lastLerpFillColour: Color? = null
        var lastLerpOutlineColour: Color? = null

        var boxList = if (CoreConfig.renders.enabled()) {
            state.getOutlineShape(mc.world, pos).boundingBoxes.toSet()
        } else {
            null
        }

        override fun toString(): String =
            "Mine Ticks: " + mineTicks.toString() +
                    "\nBreak Type: " + breakType.toString() +
                    "\nBlock Pos: " + pos.x.toString() + " " + pos.y.toString() + " " + pos.z.toString() +
                    "\nBlock: " + state.block.name.string +
                    "\nCurrent Break Delta: " + currentBreakDelta.toString() +
                    "\nBest Tool: " + bestTool.toString() + " " + mc.player?.inventory?.getStack(bestTool)?.name?.string


        val miningProgress: Float
            get() = if (CoreConfig.breakMode == BreakMode.Total) {
                mineTicks * currentBreakDelta
            } else {
                additiveBreakDelta
            }

        val previousMiningProgress: Float
            get() = if (CoreConfig.breakMode == BreakMode.Total) {
                (mineTicks - 1) * previousBreakDelta
            } else {
                additiveBreakDelta - currentBreakDelta
            }

        fun updateBreakDeltas(newBreakDelta: Float) {
            previousBreakDelta = currentBreakDelta
            currentBreakDelta = newBreakDelta
            additiveBreakDelta += newBreakDelta
        }

        fun updateRenders() {
            state.getOutlineShape(mc.world, pos)?.boundingBoxes?.toSet()
        }

        fun drawRenders(renderer: Renderer) {
            val threshold = if (breakType.isPrimary()) 2f - CoreConfig.breakThreshold else 1f
            val previousFactor = previousMiningProgress * threshold
            val nextFactor = miningProgress * threshold
            val currentFactor = LerpUtils.lerp(previousFactor, nextFactor, mc.tickDelta)

            val fillColour = if (CoreConfig.fillColourMode == ColourMode.Dynamic) {
                val lerpColour = LerpUtils.lerp(CoreConfig.startFillColour, CoreConfig.endFillColour, currentFactor.toDouble())
                lastLerpFillColour = lerpColour
                lerpColour
            } else {
                CoreConfig.staticFillColour
            }

            val outlineColour = if (CoreConfig.outlineColourMode == ColourMode.Dynamic) {
                val lerpColour =
                    LerpUtils.lerp(CoreConfig.startOutlineColour, CoreConfig.endOutlineColour, currentFactor.toDouble())
                lastLerpOutlineColour = lerpColour
                lerpColour
            } else {
                CoreConfig.staticOutlineColour
            }

            boxList?.forEach { box ->
                val positionedBox = box.offset(pos)

                val renderBox = if (CoreConfig.renderAnimation == RenderAnimation.Static) {
                    positionedBox
                } else {
                    RenderUtils.getLerpBox(positionedBox, currentFactor, CoreConfig.renderAnimation)
                }
                if (CoreConfig.renders == RenderType.Both || CoreConfig.renders == RenderType.Fill) {
                    renderer.boxSides(renderBox, fillColour)
                }
                if (CoreConfig.renders == RenderType.Both || CoreConfig.renders == RenderType.Line) {
                    renderer.boxLines(renderBox, outlineColour)
                }
            }
        }
    }
}