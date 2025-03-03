package mc.merge.handler

import mc.merge.event.EventBus
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.inventory.*
import mc.merge.module.modules.CoreConfig
import mc.merge.util.runInGame
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class InventoryHandler : IHandler {
    override var currentlyBeingUsedBy: IHandlerController?
        get() = slotActionQueue.firstOrNull()?.controller
        set(value) {}

    private val packetTracker = InventoryPacketTracker()
    val hotBarController = HotBarController(packetTracker)
    private val actionableInventory = ActionableInventory(packetTracker)

    private val slotActionQueue = mutableListOf<QueuedSlotActionController>()

    /** Aka, if an external mod or the player is controlling the selected hotbar slot */
    var externalIsUsingItem = false


    class QueuedSlotActionController (val controller: IHandlerController, var didInteractThisTick:Boolean)

    init {
        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY) {
            resetCooldowns()
            for (queuedController in slotActionQueue) {
                queuedController.didInteractThisTick = false
            }
        }
    }

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
                Direction.DOWN
            )
        )
    }

    private fun resetCooldowns() {
        if (actionableInventory.externalControlCooldown > 0) actionableInventory.externalControlCooldown--
        if (actionableInventory.swapCooldown > 0) actionableInventory.swapCooldown--
        if (actionableInventory.pickupCooldown > 0) actionableInventory.pickupCooldown--
        if (actionableInventory.quickMoveCooldown > 0) actionableInventory.quickMoveCooldown--
        if (actionableInventory.cloneCooldown > 0) actionableInventory.cloneCooldown--
        if (actionableInventory.quickCraftCooldown > 0) actionableInventory.quickCraftCooldown--
        if (actionableInventory.pickupAllCooldown > 0) actionableInventory.pickupAllCooldown--

        actionableInventory.dropsThisTick = 0
    }
}

class HotBarController(val packetTracker: InventoryPacketTracker) : IHandler {
    override var currentlyBeingUsedBy: IHandlerController?
        get() = queue.firstOrNull()?.controller
        set(value) {  }

    private val queue = mutableListOf<QueuedSelectHotbarController>()
    private var swapBackToSlot: Int? = null
    private var swapCooldown = 0
    private var useCooldown = 0

    class QueuedSelectHotbarController (val controller: IHandlerController, var onLostControl: () -> Unit = {}, var didInteractThisTick:Boolean = true)

    init {
        onInGameEvent<TickEvent.Pre>(priority = EventBus.MAX_PRIORITY) {
            if (swapCooldown > 0) swapCooldown--
            if (useCooldown > 0) useCooldown--
            queue.forEach { it.didInteractThisTick = false }
        }

        onInGameEvent<TickEvent.Post> {
            val swapToSlot = swapBackToSlot
            if (CoreConfig.swapBack.getValue() && swapCooldown == 0 && queue.size == 0 && swapToSlot != null) {
                selectSlot(swapToSlot)
                swapBackToSlot = null
            }
            queue.removeIf { !it.didInteractThisTick }
        }
    }

    fun canUse(testController: IHandlerController): Boolean {
        return queue.firstOrNull()?.controller == testController && useCooldown == 0
    }

    fun isInControl(testController: IHandlerController): Boolean {
        return queue.firstOrNull()?.controller == testController
    }

    /**
     * must be called every tick to remain in queue or remain in control
     * */
    fun trySelectingSlot(slotIndex: Int, actionController: IHandlerController, onLostControl: () -> Unit = {}) {
        val queueIndex = queue.indexOfFirst { it.controller == actionController }
        //add to queue if needed
        if (queueIndex == -1) {
            var addedToQueue = false
            queue.forEachIndexed { index, inQueue ->
                if (inQueue.controller.getPriority() < actionController.getPriority()) {
                    if (index == 0) {
                        queue.first().onLostControl.invoke()
                    }
                    queue.add(index, QueuedSelectHotbarController(actionController, onLostControl))
                    addedToQueue = true
                    return@forEachIndexed
                }
            }
            if (!addedToQueue) {
                queue.add(QueuedSelectHotbarController(actionController, onLostControl))
            }
        } else {
            queue[queueIndex].didInteractThisTick = true
        }

        //select the slot if possible
        if (queue.firstOrNull()?.controller == actionController) {
            selectSlot(slotIndex)
        }
    }

    private fun selectSlot(index: Int) {
        runInGame {
            if (player.inventory.selectedSlot == index) return@runInGame

            if (swapCooldown > 0) return@runInGame
            if (CoreConfig.swapBack.getValue() && swapBackToSlot == null) {
                swapBackToSlot = player.inventory.selectedSlot
            }

            player.inventory.selectedSlot = index
            packetTracker.sendPacket(UpdateSelectedSlotC2SPacket(index))
            swapCooldown = CoreConfig.swapHotbarCooldown.getValue()
            useCooldown = CoreConfig.useHotbarCooldown.getValue()
        }
    }

}

class InventoryPacketTracker {
    var isSendingPacket = false

    init {

    }

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

class ActionableInventory(val packetTracker: InventoryPacketTracker) {
    /** Tracks whether a packet is getting sent from this mod or from the player/external mod */
//    private var isSendingPacket = false

//    var swapBackToSlot: Int? = null
//
//    var selectOnHotbarCooldown = 0
    var swapCooldown = 0
    var externalControlCooldown = 0  //TODO: Add setting to core configs
    var pickupCooldown = 0 //TODO: Add setting to core configs
    var quickMoveCooldown = 0 //TODO: Add setting to core configs
    var cloneCooldown = 0 //TODO: Add setting to core configs
    var quickCraftCooldown = 0 //TODO: Add setting to core configs
    var pickupAllCooldown = 0 //TODO: Add setting to core configs

    var dropsThisTick = 0

    fun sendPacket(packet: Packet<*>) =
        packetTracker.sendPacket(packet)

    fun interactWithSlot(action: SlotActionType, slot: Slot, data:Int = 0) =
        packetTracker.interactWithSlot(action, slot, data)
}