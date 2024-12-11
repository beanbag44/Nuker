package me.beanbag.nuker.handlers

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.inventory.*
import me.beanbag.nuker.utils.runInGame
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

open class InventoryHandler : IHandler {
    override var currentlyBeingUsedBy: IHandlerController?
        get() = currentAction?.controller
        set(value) {}

    override var priority: Int = 0

    var currentAction: QueuedActions? = null

    private val queuedActions = mutableListOf<QueuedActions>()

    /** Aka, if an external mod or the player is controlling the selected hotbar slot */
    var externalIsUsingItem = false
    /** Tracks whether a packet is getting sent from this mod or from the player/external mod */
    private var isSendingPacket = false
    /** The hotbar slot to swap back to once the mod is no longer controlling the selectedSlot */
    var originalHotbarSlot: Int? = null

    //Cooldowns
    var externalControlCooldown = 0  //TODO: Add setting to core configs
    var selectOnHotbarCooldown = 0
    var swapCooldown = 0
    var pickupCooldown = 0 //TODO: Add setting to core configs
    var quickMoveCooldown = 0 //TODO: Add setting to core configs
    var cloneCooldown = 0 //TODO: Add setting to core configs
    var quickCraftCooldown = 0 //TODO: Add setting to core configs
    var pickupAllCooldown = 0 //TODO: Add setting to core configs

    var dropsThisTick = 0

    init {

        //onInGameEvent<PlayerSwitchedHotbarSlot> {}
        //onInGameEvent<PlayerClickedTheMouse> {}
        //onInGameEvent<PlayerMovedMouse> {}

        //onInGameEvent<externalModSwitchedSlots> {}
        //onInGameEvent<externalModInteractedWithHand> {}

        //user clickedSlot
        // ClickSlotC2SPacket

        //started using item
        // PlayerInteractItemC2SPacket

        //stopped using item
//        PlayerActionC2SPacket.Action.RELEASE_USE_ITEM
//        PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND

        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY + 1) {
            if (!mc.options.useKey.isPressed && externalIsUsingItem) {
                externalIsUsingItem = false
            }
        }

        onInGameEvent<PacketEvent.Send.Pre>{ event ->
//            val packet = event.packet
//
//            if (packet is UpdateSelectedSlotC2SPacket && BreakingHandler.breakingContexts.none { it?.bestTool == packet.selectedSlot }) {
//                ticksTillResume = 24
//            } else if (event.packet is PlayerInteractItemC2SPacket) {
//                if (player.getStackInHand(event.packet.hand).item.use(world, player, event.packet.hand).result.isAccepted) {
//                    externalIsUsingItem = true
//                    ticksTillResume = 24
//                }
//            } else if (packet is PlayerActionC2SPacket) {
//                if (packet.action == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
//                    externalIsUsingItem = false
//                }
//            }
        }
        onInGameEvent<TickEvent.Pre> {
            resetCooldowns()
        }
        onInGameEvent<TickEvent.Post> {
            runQueuedActions()
        }
    }

    fun tryInteract(
        owner: IHandlerController,
        queue: Boolean = true,
        actions: (currentInventory: InventoryState) -> List<IInventoryAction>,
        onFinished: (() -> Unit)? = null
    ): IInventoryResult {
        //Handler is already being used by higher priority controller
        if (currentAction?.controller != null && owner.getPriority() <= currentAction!!.controller.getPriority()) {
            if (queue) {
                queuedActions.add(QueuedActions(owner, actions, queue, onFinished))
                return Started()
            }
            return CantControl()
        } else if (currentAction != null && queue) { // This controller has priority over the other handler
            currentAction?.let{ if (it.queueable) queuedActions.add(0, it) else it.onFinished?.invoke()}
            currentAction = QueuedActions(owner, actions, queue, onFinished)
        }

        val potentialActions = actions(InventoryState.get())
        //loop through and see if we can do all the actions immediately, of if it needs to be started and then queued
        val mockHandler = MockInventoryHandler(this)
        var willNeedToQueue = false
        for(potentialAction in potentialActions) {
            val result = potentialAction.performAction(mockHandler)
            if (result == SlotActionResult.AWAITING_COOLDOWN) {
                willNeedToQueue = true
                break
            }
        }

        if (willNeedToQueue && !queue) {
            return CantControl()
        }

        if (willNeedToQueue && currentAction == null) {
            queuedActions.add(QueuedActions(owner, actions, queue, onFinished))
        } else {
            currentAction = QueuedActions(owner, actions, queue, onFinished)
            for(potentialAction in potentialActions) {
                if (potentialAction.performAction(this) == SlotActionResult.AWAITING_COOLDOWN) {
                    break
                }
//                if (potentialAction is SelectHotbarSlotAction) {
//
//                }
            }
            return Started()
        }
        currentAction = null
        onFinished?.invoke()
        return Interacted()
    }

    open fun interactWithSlot(action: SlotActionType, slot: Slot, data:Int = 0) {
        runInGame {
            isSendingPacket = true
            interactionManager.clickSlot(player.currentScreenHandler.syncId, slot.id, data, action, player)
            isSendingPacket = false
        }
    }

    open fun sendPacket(packet: Packet<*>) {
        runInGame {
            isSendingPacket = true
            networkHandler.sendPacket(packet)
            isSendingPacket = false
        }
    }

    fun offhandDoohickey() {
        sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos(420, 69, 420),
                Direction.UP
            )
        )
    }

    fun releaseHotbarControl(owner:IHandlerController) {
        //TODO
    }

    private fun resetCooldowns() {
        if (externalControlCooldown > 0) externalControlCooldown--
        if (swapCooldown > 0) swapCooldown--
        if (pickupCooldown > 0) pickupCooldown--
        if (quickMoveCooldown > 0) quickMoveCooldown--
        if (cloneCooldown > 0) cloneCooldown--
        if (quickCraftCooldown > 0) quickCraftCooldown--
        if (pickupAllCooldown > 0) pickupAllCooldown--

        dropsThisTick = 0
    }

    private fun runQueuedActions() {
        while (queuedActions.size > 0) {
            val queuedAction = queuedActions[0]
            for (action in queuedAction.actions(InventoryState.get())) {
                if (action.performAction(this) == SlotActionResult.AWAITING_COOLDOWN) {
                    return
                }
            }
            queuedAction.onFinished?.invoke()
            queuedActions.removeAt(0)
        }
    }

    class QueuedActions(
        val controller: IHandlerController,
        val actions: (currentInventory: InventoryState) -> List<IInventoryAction>,
        val queueable: Boolean,
        val onFinished: (() -> Unit)? = null
    )
}

class MockInventoryHandler(handler: InventoryHandler) : InventoryHandler() {
    init {
        externalControlCooldown = handler.externalControlCooldown
        selectOnHotbarCooldown = handler.selectOnHotbarCooldown
        swapCooldown = handler.swapCooldown
        pickupCooldown = handler.pickupCooldown
        quickMoveCooldown = handler.quickMoveCooldown
        cloneCooldown = handler.cloneCooldown
        quickCraftCooldown = handler.quickCraftCooldown
        pickupAllCooldown = handler.pickupAllCooldown
    }
    override fun sendPacket(packet: Packet<*>) {}

    override fun interactWithSlot(action: SlotActionType, slot: Slot, data:Int) {}
}
