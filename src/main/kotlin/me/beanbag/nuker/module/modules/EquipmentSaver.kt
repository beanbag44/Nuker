package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.utils.InGame
import me.beanbag.nuker.utils.runInGame
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType

class EquipmentSaver:Module("Equipment Saver", "Saves your tools/armor from breaking") {
    private val generalGroup = group("General", "General settings")
    val minDurability = setting(generalGroup,"Min Durability",
        "Stops tools from breaking below this threshold",
        10,
        min = 1,
        max = 1000)
    init {
        EventBus.subscribe<TickEvent.Pre>(this){ runInGame { onTick(it) }}
    }

    companion object {
        const val PLAYER_INVENTORY_START: Int = 35
        const val PLAYER_INVENTORY_END: Int = 9

        const val ARMOR_START: Int = 103
        const val ARMOR_END: Int = 100

        const val HOTBAR_START: Int = 0
        const val HOTBAR_END: Int = 8
    }

    private fun InGame.onTick(event: TickEvent.Pre) {
        for (i in HOTBAR_START..HOTBAR_END) {
            onItemDamaged(player.inventory.getStack(i))
        }

        for(i in ARMOR_START downTo ARMOR_END) {
            onItemDamaged(player.inventory.getStack(i))
        }
    }

    fun InGame.onItemDamaged(stack: ItemStack) {
        if (stack.maxDamage - stack.damage > minDurability.getValue()) {
            return
        }
        val fromSlot = moveFromSlot(stack)
        val toSlot = moveToSlot(stack)
        if (fromSlot != null && toSlot != null) {
            if ((HOTBAR_START..HOTBAR_END).contains(fromSlot)){
                interactionManager.clickSlot(0, toSlot, fromSlot, SlotActionType.SWAP, player)
            } else if ((HOTBAR_START..HOTBAR_END).contains(toSlot)) {
                interactionManager.clickSlot(0, fromSlot, toSlot, SlotActionType.SWAP, player)
            } else {
                //swap toSlot with hotbar, then swap hotbar with fromSlot, then hotbar with toSlot
                //eg:
                //inventory - good boots
                //hotbar - random item
                //boot slot - broken boots
                interactionManager.clickSlot(0, toSlot, HOTBAR_END - 1, SlotActionType.SWAP, player)
                //inventory - random item
                //hotbar - good boots
                //boot slot - broken boots
                interactionManager.clickSlot(0, fromSlot, HOTBAR_END - 1, SlotActionType.SWAP, player)
                //inventory - random item
                //hotbar - broken boots
                //boot slot - good boots
                interactionManager.clickSlot(0, toSlot, HOTBAR_END - 1, SlotActionType.SWAP, player)
                //inventory - broken boots
                //hotbar - random item
                //boot slot - good boots
            }
        }
    }

    private fun InGame.moveFromSlot(toMove: ItemStack): Int? {
        val inventory: PlayerInventory = player.inventory
        var matchingToolSlot: Int? = null
        for (i in HOTBAR_START..HOTBAR_END) {
            if (inventory.getStack(i).item === toMove.item && inventory.getStack(i).damage == toMove.damage) {
                matchingToolSlot = i
            }
        }
        for (i in ARMOR_START downTo ARMOR_END) {
            if (inventory.getStack(i).item === toMove.item && inventory.getStack(i).damage == toMove.damage) {
                matchingToolSlot = i
            }
        }
        return matchingToolSlot
    }

    private fun InGame.moveToSlot(toMove: ItemStack): Int? {
        val inventory: PlayerInventory = player.inventory

        var matchingToolSlot: Int? = null
        var emptySlot: Int? = null
        var nonBreakableSlot: Int? = null
        for (i in PLAYER_INVENTORY_START downTo PLAYER_INVENTORY_END) {
            val inventoryStack = inventory.getStack(i)
            if (inventoryStack.item === toMove.item && inventoryStack.maxDamage - inventoryStack.damage > minDurability.getValue()) {
                matchingToolSlot = i
            }
            if (inventoryStack.isEmpty) {
                emptySlot = i
            }
            if (inventoryStack.maxDamage == 0) {
                nonBreakableSlot = i
            }
        }
        return matchingToolSlot ?: (emptySlot ?: nonBreakableSlot)
    }
}