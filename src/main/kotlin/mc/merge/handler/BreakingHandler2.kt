package mc.merge.handler

import mc.merge.ModCore.breakingHandler
import mc.merge.ModCore.inventoryHandler
import mc.merge.ModCore.mc
import mc.merge.event.EventBus
import mc.merge.event.events.PacketEvent
import mc.merge.event.events.RenderEvent
import mc.merge.event.events.TickEvent
import mc.merge.event.onEvent
import mc.merge.event.onInGameEvent
import mc.merge.module.modules.CoreConfig
import mc.merge.module.modules.nuker.enumsettings.BreakType
import mc.merge.module.modules.nuker.enumsettings.ColourMode
import mc.merge.module.modules.nuker.enumsettings.RenderAnimation
import mc.merge.module.modules.nuker.enumsettings.RenderType
import mc.merge.render.IRenderer3D
import mc.merge.types.PosAndState
import mc.merge.types.TimeoutSet
import mc.merge.util.*
import mc.merge.util.BlockUtils.breakBlockWithRestrictionChecks
import mc.merge.util.BlockUtils.canReach
import mc.merge.util.BlockUtils.emulateBlockBreak
import mc.merge.util.BlockUtils.isBlockBroken
import mc.merge.util.BlockUtils.state
import mc.merge.util.InventoryUtils.getBestTool
import mc.merge.util.InventoryUtils.percentDamagePerTick
import net.minecraft.block.BlockState
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

class BreakingHandler2 : IHandler, IHandlerController{
    override var currentlyBeingUsedBy: IHandlerController? = null

    override fun getPriority(): HandlerPriority {
        return currentlyBeingUsedBy?.getPriority() ?: HandlerPriority.lowest()
    }

    val blockBreakTimeouts = TimeoutSet<BlockPos> { CoreConfig.blockBreakTimeout }
    private val queue: CopyOnWriteArrayList<BreakingContext> = CopyOnWriteArrayList()
    private var packetCounter = 0

    var primaryBreakContext:BreakingContext? = null
    var doubleBreakContext: BreakingContext? = null


    init{
        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY - 1) {
            packetCounter = 0

            if (!inventoryHandler.hotBarController.isInControl(this@BreakingHandler2)) { // aka, we lost control at some point
                primaryBreakContext?.let {
                    if (it.mineTicks != 0) {
                        abortBreakPacket(it.pos)
                        it.mineTicks = 0
                    }
                }
            }

            (primaryBreakContext?.bestToolSlot() ?: doubleBreakContext?.bestToolSlot())?.let {
                inventoryHandler.hotBarController.trySelectingSlot(it, this@BreakingHandler2)
            }

            if (inventoryHandler.hotBarController.canUse(this@BreakingHandler2)) {
                primaryBreakContext?.tick()
                doubleBreakContext?.tick()
            }

            queue.forEach {
                breakBlock(PosAndState(it.pos, it.state), it.owner, false, fromQueue = true)
            }
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

        onEvent<RenderEvent.Render3DEvent> { event ->
            primaryBreakContext?.drawRenders(event.renderer3D)
            doubleBreakContext?.drawRenders(event.renderer3D)
        }
    }

    //
    // Public - API
    //
    fun breakBlocks(blocks: List<PosAndState>, controller: IHandlerController, queueIfNeeded: Boolean = true) {
        blocks.forEach {
            breakBlock(it, controller, queueIfNeeded)
        }
    }

    fun breakBlock(block: PosAndState, controller: IHandlerController, queueIfNeeded: Boolean = true, fromQueue: Boolean = false) {
        runInGame {
            if (block.blockPos == primaryBreakContext?.pos
                || block.blockPos == doubleBreakContext?.pos
                || blockBreakTimeouts.values().contains(block.blockPos)
                || !fromQueue && queue.any { it.pos == block.blockPos }) return@runInGame

            val breakingContext = BreakingContext(block.blockPos, block.blockState, controller)

            //queue if at max contexts
            if (!canAddContexts()){
                if (queueIfNeeded && !fromQueue) {
                    queue.add(breakingContext)
                }
                return@runInGame
            }

            //queue if using a tool that doesn't match
            val bestToolSlot = getBestTool(block.blockState, block.blockPos)
            val activeToolSlot = primaryBreakContext?.bestToolSlot() ?: doubleBreakContext?.bestToolSlot()
            if (activeToolSlot != null && activeToolSlot != bestToolSlot) {
                if (queueIfNeeded && !fromQueue) {
                    queue.add(breakingContext)
                }
                return@runInGame
            }

            //queue if too many packets
            val startType = startBreakType(percentDamagePerTick(block.blockState, block.blockPos, bestToolSlot))
            val startPacketCount = packetCount(startType, primaryBreakContext != null)
            if (startPacketCount + packetCounter > CoreConfig.packetLimit) {
                if (queueIfNeeded && !fromQueue) {
                    queue.add(breakingContext)
                }
                return@runInGame
            }

            //handle tool switching
            inventoryHandler.hotBarController.trySelectingSlot(bestToolSlot, this@BreakingHandler2)
            val isToolReady = inventoryHandler.hotBarController.canUse(this@BreakingHandler2)

            if (isToolReady && startType == StartBreakType.Insta) {
                if (fromQueue) {
                    queue.removeIf { it.pos == block.blockPos }
                }
                startBreakPacket(block.blockPos)
                return@runInGame
            }

            if (fromQueue) {
                queue.removeIf { it.pos == block.blockPos }
            }
            //handle contexts
            if (primaryBreakContext == null) {
                primaryBreakContext = breakingContext
            } else if (primaryBreakContext?.let { it.mineTicks != 0 } == true) {
                doubleBreakContext = primaryBreakContext
                primaryBreakContext = breakingContext

                doubleBreakContext?.configureForDoubleBreak()
            }

            if (isToolReady) {
                primaryBreakContext?.start()
            }
        }
    }

    fun removeFromQueue(callback: (BreakingContext) -> Boolean) {
        queue.removeIf { callback(it) }
    }

    fun isBreaking(pos: BlockPos): Boolean =
        primaryBreakContext?.pos == pos || doubleBreakContext?.pos == pos

    fun isQueued(pos: BlockPos): Boolean
        = queue.any { it.pos == pos }

    fun startBreakPacket(pos: BlockPos) = runInGame {
        packetCounter++
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    fun abortBreakPacket(pos: BlockPos) = runInGame {
        packetCounter++
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    fun stopBreakPacket(pos: BlockPos) = runInGame {
        packetCounter++
        networkHandler.sendPacket(
            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)
        )
    }

    //
    // Private - implementation details
    //
    private fun canAddContexts(): Boolean {
        if (CoreConfig.doubleBreak) {
            return primaryBreakContext == null
        }
        return primaryBreakContext == null || doubleBreakContext == null
    }

    fun startBreakType(percentBreakPerTick: Float) :StartBreakType = when {
        percentBreakPerTick >= 1 -> StartBreakType.Insta
        percentBreakPerTick >= CoreConfig.breakThreshold -> StartBreakType.AdvancedInsta
        else -> StartBreakType.Normal
    }

    private fun packetCount(type: StartBreakType, willSwitchContext:Boolean):Int = when(type) {
        StartBreakType.Insta -> 2
        StartBreakType.AdvancedInsta -> if (willSwitchContext) 4 else 3
        StartBreakType.Normal -> if (willSwitchContext) 3 else 2
    }

    private fun onBlockUpdate(pos: BlockPos, state: BlockState) {
        queue.removeIf {
            it.pos == pos && isBlockBroken(it.state, state)
        }
        listOf(primaryBreakContext, doubleBreakContext).forEach { ctx ->
            if (ctx == null) return@forEach
            if (ctx.pos != pos || !isBlockBroken(ctx.state, state)) return@forEach
            ThreadUtils.runOnMainThread {
                runInGame {
                    emulateBlockBreak(pos, ctx.state)
                }
            }
            if (ctx.breakType.isPrimary()) {
                primaryBreakContext = null
            } else {
                doubleBreakContext = null
            }
        }
    }

    fun onBlockBreak(breakingContext: BreakingContext?) {
        breakingContext?.apply {
            BrokenBlockHandler.putBrokenBlock(pos, state, CoreConfig.validateBreak)
            blockBreakTimeouts.put(pos)

            if (!CoreConfig.validateBreak) {
                ThreadUtils.runOnMainThread {
                    runInGame {
                        breakBlockWithRestrictionChecks(pos)
                    }
                }
            }
        }
        nullifyBreakingContext(breakingContext)
    }

    fun nullifyBreakingContext(breakingContext: BreakingContext?) {
        if (breakingContext == primaryBreakContext) {
            primaryBreakContext = null
        } else if (breakingContext == doubleBreakContext) {
            doubleBreakContext = null
        }
    }
}

class BreakingContext(
    val pos: BlockPos,
    val state: BlockState,
    val owner: IHandlerController,
) {
    var mineTicks: Int = 0
    var breakType = BreakType.Primary
    val currentBreakDelta: Float
        get() = runInGame{percentDamagePerTick(state, pos, bestToolSlot())} ?: 0.0f
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
                "\nBest Tool: " + bestToolSlot().toString() + " " + mc.player?.inventory?.getStack(bestToolSlot())?.name?.string


    val miningProgress: Float
        get() = mineTicks * currentBreakDelta

    val previousMiningProgress: Float
        get() = (mineTicks - 1) * currentBreakDelta

    fun bestToolSlot() : Int = runInGame{getBestTool(state, pos)} ?: -1

    fun start() {
        when(startBreakType()) {
            StartBreakType.Insta -> {
                breakingHandler.stopBreakPacket(pos)
                breakingHandler.startBreakPacket(pos)
            }
            StartBreakType.AdvancedInsta -> {
                breakingHandler.stopBreakPacket(pos)
                breakingHandler.startBreakPacket(pos)
                breakingHandler.stopBreakPacket(pos)
                breakingHandler.onBlockBreak(this@BreakingContext)
            }
            StartBreakType.Normal -> {
                breakingHandler.stopBreakPacket(pos)
                breakingHandler.startBreakPacket(pos)
            }
        }
        mineTicks ++
    }

    fun startBreakType():StartBreakType =
        runInGame { breakingHandler.startBreakType(percentDamagePerTick(state, pos, bestToolSlot()))} ?: StartBreakType.Normal

    fun configureForDoubleBreak() {
        breakType = BreakType.Secondary
        breakingHandler.stopBreakPacket(pos)
    }

    fun tick() { runInGame {
        if (!canReach(player.eyePos, pos, CoreConfig.breakRadius) && breakType.isPrimary()) {
            breakingHandler.abortBreakPacket(pos)

            breakingHandler.nullifyBreakingContext(this@BreakingContext)
            return@runInGame
        }

        if (pos.state != state) {
            breakingHandler.nullifyBreakingContext(this@BreakingContext)
            return@runInGame
        }

        val threshold = if (breakType.isPrimary()) {
            CoreConfig.breakThreshold
        } else {
            1.0f
        }

        if (miningProgress >= threshold) {
            if (breakType.isPrimary()) {
                breakingHandler.stopBreakPacket(pos)
            }
            breakingHandler.onBlockBreak(this@BreakingContext)
        }
        if (!inventoryHandler.hotBarController.canUse(breakingHandler)) return@runInGame

        val hasStarted = mineTicks > 0
        if (!hasStarted) {
            start()
        } else {
            mineTicks++
        }
    }}

    fun drawRenders(iRenderer3D: IRenderer3D) {
        if (mineTicks == 0) return
        val threshold = if (breakType.isPrimary()) 2f - CoreConfig.breakThreshold else 1f
        val previousFactor = previousMiningProgress * threshold
        val nextFactor = miningProgress * threshold
        val currentFactor = LerpUtils.lerp(previousFactor, nextFactor, Versioned.tickDelta())

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
                iRenderer3D.boxSides(renderBox, fillColour)
            }
            if (CoreConfig.renders == RenderType.Both || CoreConfig.renders == RenderType.Line) {
                iRenderer3D.boxLines(renderBox, outlineColour)
            }
        }
    }
}

enum class StartBreakType {
    Insta,
    AdvancedInsta,
    Normal
}