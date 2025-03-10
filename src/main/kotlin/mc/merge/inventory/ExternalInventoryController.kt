package mc.merge.inventory

import mc.merge.event.events.PacketEvent
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.HandlerPriority
import mc.merge.handler.IHandlerController
import mc.merge.handler.InventoryHandler
import mc.merge.handler.InventoryPacketTracker
import mc.merge.util.runInGame
//? if >=1.21.2 {
/*import net.minecraft.item.consume.UseAction
*///?} else {
import net.minecraft.util.UseAction
//?}
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

class ExternalInventoryController(val inventoryHandler: InventoryHandler, val packetTracker: InventoryPacketTracker) : IHandlerController{
    var isDrawingBow = false
    var isEating = false
    var isAttacking = false
    var isDoingSomething = false
    override fun getPriority(): HandlerPriority {
        return HandlerPriority(0, false, isExternal = true)
    }

    init {
        onInGameEvent<TickEvent.Pre> {
            val currentStack = player.getStackInHand(player.activeHand)

            if (!player.isUsingItem || player.activeItem.isEmpty) {
                isDrawingBow = false
                isEating = false
                isAttacking = false
                isDoingSomething = false
            }
            if (!isDrawingBow && !isEating && !isAttacking && !isDoingSomething) {
                return@onInGameEvent
            }
            if (currentStack.useAction == UseAction.BOW) {
                isDrawingBow = true
                setInControl()
            } else if (currentStack.useAction == UseAction.EAT) {
                isEating = true
                setInControl()
            } else if (currentStack.useAction == UseAction.SPEAR) {
                isAttacking = true
                setInControl()
            }
        }

        onInGameEvent<PacketEvent.Send.Pre> {
            if (packetTracker.isSendingPacket) return@onInGameEvent
            val packet = it.packet

            if (packet is UpdateSelectedSlotC2SPacket) {
                isDoingSomething = true
                setInControl()
            } else if (packet is PlayerInteractItemC2SPacket) {
                isDoingSomething = true
                setInControl()
            } else if (packet is PlayerActionC2SPacket) {
                if (packet.action == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
                    isDoingSomething = false
                    isDrawingBow = false
                    isEating = false
                }
            }
        }

        inventoryHandler.hotBarController
    }

    private fun setInControl() {
        runInGame {
            inventoryHandler.hotBarController.trySelectingSlot(player.inventory.selectedSlot, this@ExternalInventoryController)
        }
    }

}