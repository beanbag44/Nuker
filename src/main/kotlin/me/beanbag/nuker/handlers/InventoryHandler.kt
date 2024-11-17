package me.beanbag.nuker.handlers

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.inventory.InventoryAction
import me.beanbag.nuker.module.modules.CoreConfig.swapCooldownTicks
import me.beanbag.nuker.utils.runInGame
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class InventoryHandler : IHandler {

    override var currentlyBeingUsedBy: Module? = null
    override var priority: Int = 0

    private var ticksTillResume = 0
    var externalIsUsingItem = false
    /** Keeps track of whether a packet is getting sent from this mod or from the player/external mod */
    var isSendingPacket = false

    var swapCoolDown = 0
    var dropsThisTick = 0

    private val pendingInventorySlotActions = mutableListOf<InventorySlotAction>()

    fun externalInControl() = /*ticksTillResume > 0 ||*/ externalIsUsingItem

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
            if (ticksTillResume > 0) {
                ticksTillResume--
            }
            if (!mc.options.useKey.isPressed && externalIsUsingItem) {
                externalIsUsingItem = false
            }
        }

        onInGameEvent<PacketEvent.Send.Pre>{ event ->
            val packet = event.packet

            if (packet is UpdateSelectedSlotC2SPacket && BreakingHandler.breakingContexts.none { it?.bestTool == packet.selectedSlot }) {
                ticksTillResume = 24
            } else if (event.packet is PlayerInteractItemC2SPacket) {
                if (player.getStackInHand(event.packet.hand).item.use(world, player, event.packet.hand).result.isAccepted) {
                    externalIsUsingItem = true
                    ticksTillResume = 24
                }
            } else if (packet is PlayerActionC2SPacket) {
                if (packet.action == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
                    externalIsUsingItem = false
                }
            }
        }
    }

    private fun tickReset() {
        ticksTillResume--
        swapCoolDown--
        dropsThisTick = 0
    }

    fun interact(action: InventoryAction) {
        when (action) {
            is SwapAction -> {
                swapCoolDown = swapCooldownTicks.getValue()
            }
            is DropAction -> {
                //drop
            }
            is SelectOnHotbarAction -> {
                //update selected
            }
            is MoveAndDiscardIfNeededAction -> {
                //move and discard
            }
        }
    }

    private fun swap(action: SwapAction) {

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

    private fun sendPacket(packet: Packet<*>) {
        runInGame {
            isSendingPacket = true
            networkHandler.sendPacket(packet)
            isSendingPacket = false
        }
    }

    private fun interactWithSlot(action: SlotActionType, slot: Slot, slot2: Slot? = null) {
        runInGame {
            isSendingPacket = true
            interactionManager.clickSlot(player.playerScreenHandler.syncId, slot.id, slot2?.index ?: 0, action, player)
            isSendingPacket = false
        }
    }

    private class InventorySlotAction(val action:SlotActionType, val slot1:Slot, val slot2: Slot? = null)
}


interface InventoryAction

class SwapAction(var fromSlot: Slot, var toSlotSlot: Slot) : InventoryAction

class DropAction(var slot: Slot, var all: Boolean = false) : InventoryAction

class SelectOnHotbarAction(var hotbarIndex: Int) : InventoryAction

class MoveAndDiscardIfNeededAction(var fromSlot: Slot, var toSlot: Slot) : InventoryAction