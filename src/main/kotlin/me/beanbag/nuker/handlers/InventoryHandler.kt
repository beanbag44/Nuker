package me.beanbag.nuker.handlers

import me.beanbag.nuker.eventsystem.EventBus
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

class InventoryHandler :IHandler{
    override var currentlyBeingUsedBy: IHandlerController?
        get() = selectHotbarQueue.firstOrNull()?.controller
        set(value) {}

    val selectHotbarQueue = mutableListOf<QueuedSelectHotbarController>()
    val slotActionQueue = mutableListOf<QueuedSlotActionController>()

    /** Aka, if an external mod or the player is controlling the selected hotbar slot */
    var externalIsUsingItem = false

    private val actionableInventory = ActionableInventory()

    class QueuedSlotActionController (val controller: IHandlerController, var didInteractThisTick:Boolean)
    class QueuedSelectHotbarController (val controller: IHandlerController, var action:SelectHotbarSlotAction)

    init {
        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY) {
            resetCooldowns()
            for (queuedController in slotActionQueue) {
                queuedController.didInteractThisTick = false
            }
        }

        onInGameEvent<TickEvent.Post> {
            if (actionableInventory.selectOnHotbarCooldown == 0 && selectHotbarQueue.size == 0 && actionableInventory.swapBackToSlot != null) {
                val result = SelectHotbarSlotAction(actionableInventory.swapBackToSlot!!).performAction(actionableInventory)
                if (result == SlotActionResult.SUCCESS) {
                    actionableInventory.swapBackToSlot = null
                }
            }
            slotActionQueue.removeIf { !it.didInteractThisTick }
        }
    }


    fun selectSlot(owner: IHandlerController, action:SelectHotbarSlotAction): IInventoryResult {
        val queueIndex = selectHotbarQueue.indexOfFirst { it.controller == owner }
        //add to the queue and return
        if (queueIndex == -1 && action.retainControl) {
            var addedToQueue = false
            selectHotbarQueue.forEachIndexed { index, queuedSelectHotbarController ->
                if (queuedSelectHotbarController.controller.getPriority() < owner.getPriority()) {
                    if (index == 0) {
                        selectHotbarQueue.first().action.onLostControl.invoke()
                    }
                    selectHotbarQueue.add(index, QueuedSelectHotbarController(owner, action))
                    addedToQueue = true
                    return@forEachIndexed
                }
            }
            if (!addedToQueue) {
                selectHotbarQueue.add(QueuedSelectHotbarController(owner, action))
            }
        }

        if (selectHotbarQueue.size == 0 ||
            selectHotbarQueue.first().controller == owner ||
            selectHotbarQueue.first().controller.getPriority() < owner.getPriority()
        ) {
            val result = action.performAction(actionableInventory)
            return when (result) {
                SlotActionResult.SUCCESS -> Interacted()
                SlotActionResult.AWAITING_COOLDOWN -> AwaitingCooldown()
            }
        }
        return CantControl()
    }

    fun releaseSlot(owner: IHandlerController) = selectHotbarQueue.removeIf { it.controller == owner }

    /**
     * @param controller The controller that is trying to interact with the inventory
     * @param action The action that the controller is trying to perform
     * @param hasMoreActions Whether the controller is done interacting with the inventory. If true, the controller will stay in control until next tick
     */
    fun interact(controller: IHandlerController, action: SlotAction, hasMoreActions: Boolean = false): IInventoryResult {
        //add to queue
        val queueIndex = slotActionQueue.indexOfFirst { it.controller == controller }
        if (queueIndex == -1) {
            var addedToQueue = false
            slotActionQueue.forEachIndexed { index, queuedController ->
                if (queuedController.controller.getPriority() < controller.getPriority()) {
                    slotActionQueue.add(index, QueuedSlotActionController(controller, true))
                    addedToQueue = true
                    return@forEachIndexed
                }
            }
            if (!addedToQueue) {
                slotActionQueue.add(QueuedSlotActionController(controller, true))
            }
        }
        if (slotActionQueue.first().controller == controller) {
            val result = action.performAction(actionableInventory)
            slotActionQueue.first().didInteractThisTick = true
            return when (result) {
                SlotActionResult.SUCCESS -> {
                    if (!hasMoreActions) {
                        slotActionQueue.removeAt(0)
                    }
                    Interacted()
                }
                SlotActionResult.AWAITING_COOLDOWN -> AwaitingCooldown()
            }
        } else {
            val matchingQueueItem = slotActionQueue.firstOrNull { it.controller == controller }
            matchingQueueItem?.didInteractThisTick = true
            return CantControl()
        }
    }

    fun offhandDoohickey() {
        actionableInventory.sendPacket(
            PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos(420, 69, 420),
                Direction.UP
            )
        )
    }

    private fun resetCooldowns() {
        if (actionableInventory.externalControlCooldown > 0) actionableInventory.externalControlCooldown--
        if (actionableInventory.selectOnHotbarCooldown > 0) actionableInventory.selectOnHotbarCooldown--
        if (actionableInventory.swapCooldown > 0) actionableInventory.swapCooldown--
        if (actionableInventory.pickupCooldown > 0) actionableInventory.pickupCooldown--
        if (actionableInventory.quickMoveCooldown > 0) actionableInventory.quickMoveCooldown--
        if (actionableInventory.cloneCooldown > 0) actionableInventory.cloneCooldown--
        if (actionableInventory.quickCraftCooldown > 0) actionableInventory.quickCraftCooldown--
        if (actionableInventory.pickupAllCooldown > 0) actionableInventory.pickupAllCooldown--

        actionableInventory.dropsThisTick = 0
    }
}

class ActionableInventory {
    /** Tracks whether a packet is getting sent from this mod or from the player/external mod */
    private var isSendingPacket = false

    var swapBackToSlot: Int? = null

    var selectOnHotbarCooldown = 0
    var swapCooldown = 0
    var externalControlCooldown = 0  //TODO: Add setting to core configs
    var pickupCooldown = 0 //TODO: Add setting to core configs
    var quickMoveCooldown = 0 //TODO: Add setting to core configs
    var cloneCooldown = 0 //TODO: Add setting to core configs
    var quickCraftCooldown = 0 //TODO: Add setting to core configs
    var pickupAllCooldown = 0 //TODO: Add setting to core configs

    var dropsThisTick = 0

    fun sendPacket(packet: Packet<*>) {
        runInGame {
            isSendingPacket = true
            networkHandler.sendPacket(packet)
            isSendingPacket = false
        }
    }

    fun interactWithSlot(action: SlotActionType, slot: Slot, data:Int = 0) {
        runInGame {
            isSendingPacket = true
            interactionManager.clickSlot(player.currentScreenHandler.syncId, slot.id, data, action, player)
            isSendingPacket = false
        }
    }
}