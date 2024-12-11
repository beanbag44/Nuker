package me.beanbag.nuker.inventory

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.handlers.InventoryHandler
import me.beanbag.nuker.module.modules.CoreConfig
import me.beanbag.nuker.utils.runInGame
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType


interface IInventoryAction {
    fun performAction(handler:InventoryHandler) : SlotActionResult
}

enum class SlotActionResult {
    SUCCESS,
    AWAITING_COOLDOWN
}

class SelectHotbarSlotAction(private val index: Int): IInventoryAction {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (runInGame { return@runInGame if (player.inventory.selectedSlot == index) SlotActionResult.SUCCESS else null } != null) {
            return SlotActionResult.SUCCESS
        }
        if (handler.selectOnHotbarCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.originalHotbarSlot = mc.player?.inventory?.selectedSlot
        mc.player?.inventory?.selectedSlot = index
        handler.sendPacket(UpdateSelectedSlotC2SPacket(index))
        handler.selectOnHotbarCooldown = CoreConfig.selectOnHotbarCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

abstract class SlotAction: IInventoryAction

/** Performs a normal slot click. This can pickup or place items in the slot, possibly merging the cursor stack into the slot, or swapping the slot stack with the cursor stack if they can't be merged.*/
class PickupAction(private val slot: Slot) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.pickupCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.PICKUP, slot)
//        handler.pickupCooldown = CoreConfig.pickupCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

/** Performs a shift-click. This usually quickly moves items between the player's inventory and the open screen handler.*/
class QuickMoveAction(val slot: Slot) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.quickMoveCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.QUICK_MOVE, slot)
//        handler.quickMoveCooldown = CoreConfig.quickMoveCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

/**
 * Exchanges items between a slot and a hotbar slot. This is usually triggered by the player pressing a 1-9 number key while hovering over a slot.
 *
 * When the action type is swap, the click data is the hotbar slot to swap with (0-8).
 */
class SwapAction(val slot: Slot, val hotbarIndex:Int) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.swapCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.SWAP, slot, hotbarIndex)
//        handler.swapCooldown = CoreConfig.swapCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

/** Clones the item in the slot. Usually triggered by middle clicking an item in creative mode. */
class CloneAction(val slot: Slot) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.cloneCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.CLONE, slot)
//        handler.cloneCooldown = CoreConfig.cloneCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

/** Throws the item out of the inventory. This is usually triggered by the player pressing Q while hovering over a slot, or clicking outside the window.
 *
 * When the action type is throw, the click data determines whether to throw a whole stack (1) or a single item from that stack (0). */
class DropAction(private val slot: Slot, private val fullStack:Boolean) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.dropsThisTick >= CoreConfig.maxDropsPerTick.getValue()) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.THROW, slot, if (fullStack) 1 else 0)
        handler.dropsThisTick++
        return SlotActionResult.SUCCESS
    }
}

/** Drags items between multiple slots. This is usually triggered by the player clicking and dragging between slots.
 *
 * This action happens in 3 stages. Stage 0 signals that the drag has begun, and stage 2 signals that the drag has ended. In between multiple stage 1s signal which slots were dragged on.
 *
 * The stage is packed into the click data along with the mouse button that was clicked. See {@link net.minecraft.screen.ScreenHandler#packQuickCraftData(int, int) ScreenHandler.packQuickCraftData(int, int)} for details. */
class QuickCraftAction(val slot: Slot, val stage:Int, val mouseButton:Int) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.quickCraftCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN

        handler.interactWithSlot(SlotActionType.QUICK_CRAFT, slot, ScreenHandler.packQuickCraftData(stage, mouseButton))
//        handler.quickCraftCooldown = CoreConfig.quickCraftCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}

/** Replenishes the cursor stack with items from the screen handler. This is usually triggered by the player double clicking. */
class PickupAllAction(private val slot: Slot) : SlotAction() {
    override fun performAction(handler: InventoryHandler): SlotActionResult {
        if (handler.pickupAllCooldown > 0) return SlotActionResult.AWAITING_COOLDOWN
        handler.interactWithSlot(SlotActionType.QUICK_CRAFT, slot)
//        handler.pickupAllCooldown = CoreConfig.pickupAllCooldown.getValue()
        return SlotActionResult.SUCCESS
    }
}


//class SwapAction(private var fromSlot: Slot, private var toSlot: Slot) : ExtendedInventoryAction {
//    private val queue = mutableListOf<SwapAction>()
//    private var result: StartedInteraction? = null
//    private var handler: InventoryHandler? = null
//
//    override fun runNextInQueue() : InventoryResult {
//        val result = queue.first().performAction(handler!!)
//        if (result is Error) {
//            this.result!!.finish()
//        }
//        if (result is Interacted) {
//            queue.removeAt(0)
//            if (queue.isEmpty()) {
//                this.result!!.finish()
//            }
//        }
//        return result
//    }
//
//    override fun performAction(handler: InventoryHandler): InventoryResult {
//        if (handler.swapCoolDown > 0) {
//            return CantControl()
//        }
//
//        val fromIsHotbar = fromSlot.index in 0..8
//        val toIsHotbar = toSlot.index in 0..8
//        if (!fromIsHotbar && !toIsHotbar) {
//            this@SwapAction.handler = handler
//            return runInGame {
//                val hotbarSlot = player.currentScreenHandler.getSlot(CoreConfig.usableHotbarSlot.getValue() - 1)
//                val restrictedSlot = if (fromSlot.canInsert(hotbarSlot.stack)) toSlot else fromSlot
//                val unrestrictedSlot = if (fromSlot == restrictedSlot) toSlot else fromSlot
//
//                handler.interactWithSlot(SlotActionType.SWAP, unrestrictedSlot, data = hotbarSlot.index)
//                handler.swapCooldown = CoreConfig.swapCooldownTicks.getValue()
//
//                queue.add(SwapAction(hotbarSlot, restrictedSlot))
//                queue.add(SwapAction(hotbarSlot, unrestrictedSlot))
//                handler.pendingActions.add(this@SwapAction)
//
//                result = StartedInteraction()
//                return@runInGame result!!
//            } ?: CantControl()
//        }
//        if (fromIsHotbar) {
//            handler.interactWithSlot(SlotActionType.SWAP, toSlot, fromSlot)
//            return Interacted()
//        } else { //toIsHotbar
//            handler.interactWithSlot(SlotActionType.SWAP, fromSlot, toSlot)
//            return Interacted()
//        }
//    }
//}