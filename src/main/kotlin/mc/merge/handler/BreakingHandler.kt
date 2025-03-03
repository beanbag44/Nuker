//package mc.merge.handler
//
//import mc.merge.ModCore.breakingHandler
//import mc.merge.ModCore.inventoryHandler
//import mc.merge.event.events.PacketEvent
//import mc.merge.event.events.RenderEvent
//import mc.merge.event.events.TickEvent
//import mc.merge.event.onEvent
//import mc.merge.event.onInGameEvent
//import mc.merge.module.modules.CoreConfig
//import mc.merge.module.modules.nuker.enumsettings.*
//import mc.merge.render.IRenderer3D
//import mc.merge.types.PosAndState
//import mc.merge.types.TimeoutSet
//import mc.merge.util.*
//import mc.merge.util.BlockUtils.breakBlockWithRestrictionChecks
//import mc.merge.util.BlockUtils.canReach
//import mc.merge.util.BlockUtils.emulateBlockBreak
//import mc.merge.util.BlockUtils.isBlockBroken
//import mc.merge.util.BlockUtils.state
//import mc.merge.util.InventoryUtils.getBestTool
//import mc.merge.util.InventoryUtils.percentDamagePerTick
//import mc.merge.ModCore.mc
//import net.minecraft.block.BlockState
//import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
//import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
//import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import java.awt.Color
//import java.util.concurrent.CopyOnWriteArrayList
//
//object BreakingHandler : IHandler, IHandlerController {
//    override var currentlyBeingUsedBy: IHandlerController? = null
//
//    override fun getPriority(): HandlerPriority {
//        return currentlyBeingUsedBy?.getPriority() ?: HandlerPriority.lowest()
//    }
//
//    val blockBreakTimeouts = TimeoutSet<BlockPos> { CoreConfig.blockBreakTimeout }
//    val queue: CopyOnWriteArrayList<BreakingContext> = CopyOnWriteArrayList()
//
//    private var packetCounter = 0
//
//    var primaryBreakContext:BreakingContext? = null
//    var doubleBreakContext: BreakingContext? = null
//
//
//    init {
//        onInGameEvent<TickEvent.Pre>(99) {
//            packetCounter = 0
//            if (!inventoryHandler.hotBarController.isInControl(this@BreakingHandler)) {
//                primaryBreakContext = null
//                //Can't stop the double break, so just let it do its thing
//                return@onInGameEvent
//            }
//            //TODO start the primary break context if needed
////            primaryBreakContext?.tick()
////            doubleBreakContext?.tick()
//        }
//
//        onInGameEvent<TickEvent.Post> {
//            if (primaryBreakContext == null && doubleBreakContext == null && packetCounter == 0) {
//                inventoryHandler.releaseSlot(this@BreakingHandler)
//            }
//
//            updateBreakingContexts()
//
//        }
//
//        onEvent<PacketEvent.Receive.Pre>{ event ->
//            val packet = event.packet
//
//            if (packet is BlockUpdateS2CPacket) {
//                onBlockUpdate(packet.pos, packet.state)
//            } else if (packet is ChunkDeltaUpdateS2CPacket) {
//                packet.visitUpdates { pos, state ->
//                    onBlockUpdate(pos, state)
//                }
//            }
//        }
//
//        onEvent<RenderEvent.Render3DEvent> { event ->
//            primaryBreakContext?.drawRenders(event.renderer3D)
//            doubleBreakContext?.drawRenders(event.renderer3D)
//        }
//    }
//
//    fun onLostControl() {
//
//    }
//
//    private fun canUseInventory(): Boolean {
//        val inventoryPriority = inventoryHandler.currentlyBeingUsedBy?.getPriority()
//        return !(inventoryPriority != null && inventoryPriority > (currentlyBeingUsedBy?.getPriority() ?: HandlerPriority.lowest()))
//    }
//
//    fun InGame.checkAttemptBreaks(blockVolume: List<PosAndState>, owner:IHandlerController): List<PosAndState> {
//        if (!canUseInventory()) {
//            return emptyList()
//        }
//
//        val startedBlocks = mutableListOf<PosAndState>()
//        blockVolume.forEach { block ->
//            if (breakBlock(block, owner)) startedBlocks.add(block)
//            else if (cantBreakMore()) return@forEach
//        }
//        return startedBlocks
//    }
//
//    fun tryBreaks(blocks: List<PosAndState>, owner:IHandlerController, queueIfNeeded:Boolean = false) : List<PosAndState> {
//        var contexts = blocks.map { createContext(it, owner) }.toMutableList()
//        contexts.removeIf { primaryBreakContext?.pos == it.pos || doubleBreakContext?.pos == it.pos }
//        if ()
//    }
//
//    private fun InGame.breakBlock(block: PosAndState, owner: IHandlerController): Boolean {
//        if (!hasOpenBreakingContext()
//            || primaryBreakContext?.pos == block.blockPos
//            || doubleBreakContext?.pos == block.blockPos
//            ) return false
//
//        //TODO: handle tool switching if needed
//        if (/* inventory handler is switching slots
//            ||*/primaryBreakContext != null && primaryBreakContext?.bestToolSlot() != player.inventory.selectedSlot
//            ) return false
//
//        val startType = startBreakType(percentDamagePerTick(block.blockState, block.blockPos, getBestTool(block.blockState, block.blockPos)))
//
//        val startPacketCount = packetCount(startType, primaryBreakContext != null)
//        if (startPacketCount + packetCounter > CoreConfig.packetLimit) {
//            //TODO: if we need to switch context and we are still under the packet limit, start a new break context and swap
//            return false
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////        val bestTool = getBestTool(block.blockState, blockPos)
////
////        primaryBreakContext?.run {
////            if (this.bestTool != bestTool) return false
////        }
////
////        val percentBreak = percentDamagePerTick(block.blockState, blockPos, bestTool)
////        val isInstaBreak = percentBreak >= 1
////        val breakPacketCount = if (isInstaBreak) 1 else 2
////
////        packetCounter += breakPacketCount
////
////        if (packetCounter > CoreConfig.packetLimit) return false
////
////        if (isInstaBreak) {
////            startBreakPacket(blockPos)
////            return true
////        }
////
////        if (primaryBreakContext != null) {
////            primaryBreakContext?.breakType = BreakType.Secondary
////            doubleBreakContext = primaryBreakContext
////            doubleBreakContext?.let {
////                stopBreakPacket(it.pos)
////            }
////        }
////
////        primaryBreakContext = BreakingContext(
////            blockPos,
////            block.blockState,
////            percentBreak,
////            bestTool,
////            owner,
////        ).apply {
////            mineTicks++
////        }
////
////        if (doubleBreakContext == null) {
////            updateSelectedSlot()
////        }
////
////        startPacketBreaking(blockPos)
////
//////        if (percentBreak >= CoreConfig.breakThreshold) {
//////            stopBreakPacket(blockPos)
//////            onBlockBreak(primaryBreakContext)
//////        }
////
////        return true
//    }
//
//    private fun createContext(block:PosAndState, owner: IHandlerController): BreakingContext = BreakingContext(
//        block.blockPos,
//        block.blockState,
//        owner
//    )
//
//    private fun InGame.onBlockBreak(breakingContext: BreakingContext?) {
//        breakingContext?.apply {
//            BrokenBlockHandler.putBrokenBlock(pos, state, CoreConfig.validateBreak)
//            blockBreakTimeouts.put(pos)
//
//            if (!CoreConfig.validateBreak) {
//                ThreadUtils.runOnMainThread {
//                    breakBlockWithRestrictionChecks(pos)
//                }
//            }
//        }
//        nullifyBreakingContext(breakingContext)
//    }
//
//    private fun InGame.startPacketBreaking(pos: BlockPos) {
//        stopBreakPacket(pos)
//        startBreakPacket(pos)
//    }
//
//    private fun InGame.startBreakPacket(pos: BlockPos) {
//        networkHandler.sendPacket(
//            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
//        )
//    }
//
//    private fun InGame.abortBreakPacket(pos: BlockPos) {
//        networkHandler.sendPacket(
//            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, Direction.UP)
//        )
//    }
//
//    private fun InGame.stopBreakPacket(pos: BlockPos) {
//        networkHandler.sendPacket(
//            PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)
//        )
//    }
//
//    private fun cantBreakMore(): Boolean {
//        return if (CoreConfig.doubleBreak) {
//            primaryBreakContext != null && doubleBreakContext != null
//        } else {
//            primaryBreakContext != null
//        }
//    }
//
//    private fun hasOpenBreakingContext(): Boolean =
//        if (CoreConfig.doubleBreak) primaryBreakContext == null || doubleBreakContext == null
//        else primaryBreakContext == null
//
//    private fun nullifyBreakingContext(breakingContext: BreakingContext?) {
//        if (primaryBreakContext == breakingContext) {
//            primaryBreakContext = null
//        } else if (doubleBreakContext == breakingContext) {
//            doubleBreakContext = null
//        }
//    }
//
//    private fun InGame.updateBreakingContexts() {
//        listOf(primaryBreakContext, doubleBreakContext).forEach { it?.apply {
//
//            if (!canReach(player.eyePos, pos, CoreConfig.breakRadius) && breakType.isPrimary()) {
//                abortBreakPacket(pos)
//
//                nullifyBreakingContext(this)
//                return@forEach
//            }
//
//            if (pos.state != state) {
//                nullifyBreakingContext(this)
//                return@forEach
//            }
//            mineTicks++
//            bestTool = getBestTool(state, pos)
//            if (breakType.isPrimary()) updateSelectedSlot()
//            updateBreakDeltas(percentDamagePerTick(state, pos, bestTool))
//
//            val threshold = if (breakType.isPrimary()) {
//                CoreConfig.breakThreshold
//            } else {
//                1.0f
//            }
//
//            if (miningProgress > threshold) {
//                stopBreakPacket(pos)
//                packetCounter++
//                onBlockBreak(this)
//            }
//        }}
//    }
//
//    private fun onBlockUpdate(pos: BlockPos, state: BlockState) {
//        listOf(primaryBreakContext, doubleBreakContext).forEach { ctx ->
//            if (ctx == null) return@forEach
//            if (ctx.pos != pos || !isBlockBroken(ctx.state, state)) return@forEach
//            ThreadUtils.runOnMainThread {
//                runInGame {
//                    emulateBlockBreak(pos, ctx.state)
//                }
//            }
//            nullifyBreakingContext(ctx)
//        }
//    }
//
//    private fun InGame.updateSelectedSlot() {
////        listOf(primaryBreakContext, doubleBreakContext).firstOrNull()?.run {
////            if (player.inventory.selectedSlot == bestTool) {
////                return
////            }
////            val result = inventoryHandler.selectSlot(this@BreakingHandler, SelectHotbarSlotAction(bestTool, retainControl =  true))
////            if (result is Interacted) {
////                packetCounter++
////            }
////        }
//    }
//
//    private fun startBreakType(percentBreakPerTick: Float) :StartBreakType = when {
//        percentBreakPerTick >= 1 -> StartBreakType.Insta
//        percentBreakPerTick >= CoreConfig.breakThreshold -> StartBreakType.AdvancedInsta
//        else -> StartBreakType.Normal
//    }
//
//    private fun packetCount(type: StartBreakType, willSwitchContext:Boolean):Int = when(type) {
//        StartBreakType.Insta -> 1
//        StartBreakType.AdvancedInsta -> if (willSwitchContext) 4 else 3
//        StartBreakType.Normal -> if (willSwitchContext) 3 else 2
//    }
//
//}
//
//
//class BreakingContext(
//    val pos: BlockPos,
//    val state: BlockState,
//    val owner: IHandlerController,
//) {
//    var mineTicks: Int = 0
//    var breakType = BreakType.Primary
//    var currentBreakDelta = 0f
//    var lastLerpFillColour: Color? = null
//    var lastLerpOutlineColour: Color? = null
//
//    var boxList = if (CoreConfig.renders.enabled()) {
//        state.getOutlineShape(mc.world, pos).boundingBoxes.toSet()
//    } else {
//        null
//    }
//
//    override fun toString(): String =
//        "Mine Ticks: " + mineTicks.toString() +
//                "\nBreak Type: " + breakType.toString() +
//                "\nBlock Pos: " + pos.x.toString() + " " + pos.y.toString() + " " + pos.z.toString() +
//                "\nBlock: " + state.block.name.string +
//                "\nCurrent Break Delta: " + currentBreakDelta.toString() +
//                "\nBest Tool: " + bestToolSlot().toString() + " " + mc.player?.inventory?.getStack(bestToolSlot())?.name?.string
//
//
//    val miningProgress: Float
//        get() = mineTicks * currentBreakDelta
//
//    val previousMiningProgress: Float
//        get() = (mineTicks - 1) * currentBreakDelta
//
//    fun bestToolSlot() : Int = runInGame{getBestTool(state, pos)} ?: -1
//
//    fun updateBreakDeltas(newBreakDelta: Float) {
//        currentBreakDelta = newBreakDelta
//    }
//
//    fun start() {
//        when(startBreakType()) {
//            StartBreakType.Insta -> {
//                breakingHandler.startBreakPacket(pos)
//            }
//            StartBreakType.AdvancedInsta -> {
//                breakingHandler.stopBreakPacket(pos)
//                breakingHandler.startBreakPacket(pos)
//                breakingHandler.stopBreakPacket(pos)
//            }
//            StartBreakType.Normal -> {
//                breakingHandler.stopBreakPacket(pos)
//                breakingHandler.startBreakPacket(pos)
//            }
//        }
//        mineTicks ++
//    }
//
//    fun startBreakType():StartBreakType =
//        runInGame { breakingHandler.startBreakType(percentDamagePerTick(state, pos, bestToolSlot()))} ?: StartBreakType.Normal
//
//    fun configureForDoubleBreak() {
//        breakType = BreakType.Secondary
//        breakingHandler.stopBreakPacket(pos)
//    }
//
//    fun tick() {
//        if (!inventoryHandler.hotBarController.canUse(breakingHandler)) return
//
//        val hasStarted = mineTicks > 0
//        if (!hasStarted) {
//            start()
//        } else {
//            mineTicks++
//        }
//    }
//
//    fun drawRenders(iRenderer3D: IRenderer3D) {
//        if (mineTicks == 0) return
//        val threshold = if (breakType.isPrimary()) 2f - CoreConfig.breakThreshold else 1f
//        val previousFactor = previousMiningProgress * threshold
//        val nextFactor = miningProgress * threshold
//        val currentFactor = LerpUtils.lerp(previousFactor, nextFactor, Versioned.tickDelta())
//
//        val fillColour = if (CoreConfig.fillColourMode == ColourMode.Dynamic) {
//            val lerpColour = LerpUtils.lerp(CoreConfig.startFillColour, CoreConfig.endFillColour, currentFactor.toDouble())
//            lastLerpFillColour = lerpColour
//            lerpColour
//        } else {
//            CoreConfig.staticFillColour
//        }
//
//        val outlineColour = if (CoreConfig.outlineColourMode == ColourMode.Dynamic) {
//            val lerpColour =
//                LerpUtils.lerp(CoreConfig.startOutlineColour, CoreConfig.endOutlineColour, currentFactor.toDouble())
//            lastLerpOutlineColour = lerpColour
//            lerpColour
//        } else {
//            CoreConfig.staticOutlineColour
//        }
//
//        boxList?.forEach { box ->
//            val positionedBox = box.offset(pos)
//
//            val renderBox = if (CoreConfig.renderAnimation == RenderAnimation.Static) {
//                positionedBox
//            } else {
//                RenderUtils.getLerpBox(positionedBox, currentFactor, CoreConfig.renderAnimation)
//            }
//            if (CoreConfig.renders == RenderType.Both || CoreConfig.renders == RenderType.Fill) {
//                iRenderer3D.boxSides(renderBox, fillColour)
//            }
//            if (CoreConfig.renders == RenderType.Both || CoreConfig.renders == RenderType.Line) {
//                iRenderer3D.boxLines(renderBox, outlineColour)
//            }
//        }
//    }
//}
//
//enum class StartBreakType {
//    Insta,
//    AdvancedInsta,
//    Normal
//}