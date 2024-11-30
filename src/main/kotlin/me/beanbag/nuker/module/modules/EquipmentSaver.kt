package me.beanbag.nuker.module.modules

import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.external.meteor.MeteorModule
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.utils.InGame
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket
import net.minecraft.screen.slot.SlotActionType

class EquipmentSaver : Module("Equipment Saver", "Saves your tools/armor from breaking") {
    private val generalGroup = group("General", "General settings")
    private val minDurability = setting(generalGroup,
        "Min Durability",
        "Stops tools from breaking below this threshold",
        10,
        null,
        { true },
        1, 50,
        1, 50)

    private val allowedItems = setting(generalGroup,
        "Allowed Items",
        "Items that will be saved",
        mutableListOf(
            Items.NETHERITE_AXE, Items.NETHERITE_HOE, Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET, Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_SWORD,
            Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD,)
       )

    init {
        onInGameEvent<PacketEvent.Receive.Pre> {
            //This packet doesn't update the damage, but it is called when the server sends a response to a player action
            // ... might not work for armor
            if (it.packet is PlayerActionResponseS2CPacket || it.packet is EntityDamageS2CPacket) {
                for (i in HOTBAR_START..HOTBAR_END) {
                    onItemDamaged(player.inventory.getStack(i))
                }

                for(i in ARMOR_START..ARMOR_END) {
                    onItemDamaged(player.inventory.getStack(i))
                }
            }
        }
    }

    companion object {
        const val PLAYER_INVENTORY_START: Int = 35
        const val PLAYER_INVENTORY_END: Int = 9

        const val ARMOR_START: Int = 36
        const val ARMOR_END: Int = 39

        const val HOTBAR_START: Int = 0
        const val HOTBAR_END: Int = 8
    }

    fun InGame.onItemDamaged(stack: ItemStack) {
        if (stack.item !in allowedItems.getValue()) {
            return
        }
        if (stack.maxDamage - stack.damage > minDurability.getValue()) {
            return
        }
        val fromSlot = moveFromSlot(stack)
        val toSlot = moveToSlot(stack)
        ChatHandler.sendChatLine("Moving ${stack.name.string} from slot $fromSlot to slot $toSlot")
        if (fromSlot != null && toSlot != null) {
            if ((HOTBAR_START..HOTBAR_END).contains(fromSlot)){
                swap(fromSlot, toSlot)
            } else if ((HOTBAR_START..HOTBAR_END).contains(toSlot)) {
                swap(toSlot, fromSlot)
            } else {
                //swap toSlot with hotbar, then swap hotbar with fromSlot, then hotbar with toSlot
                //other swaps are currently commented out until there's a good inventory handler
                //eg:
                //boot slot - 🔴broken boots
                //inventory - 🟢good boots  🔁
                //hotbar -    🟤random item 🔁
                swap(toSlot, HOTBAR_END - 1)
                //boot slot - 🔴broken boots🔁
                //inventory - 🟤random item
                //hotbar -    🟢good boots  🔁
                //swap(HOTBAR_END - 1, fromSlot)
                //boot slot - 🟢good boots
                //inventory - 🟤random item 🔁
                //hotbar -    🔴broken boots🔁
                //swap(HOTBAR_END - 1, toSlot)
                //boot slot - 🟢good boots
                //inventory - 🔴broken boots
                //hotbar -    🟤random item
            }
        }
    }

    private fun InGame.moveFromSlot(toMove: ItemStack): Int? {
        val inventory: PlayerInventory = player.inventory
        var matchingToolSlot: Int? = null
        for (i in HOTBAR_START..HOTBAR_END) {
            if (inventory.getStack(i).item == toMove.item && inventory.getStack(i).damage == toMove.damage) {
                matchingToolSlot = i
            }
        }
        for (i in ARMOR_START..ARMOR_END) {
            if (inventory.getStack(i).item == toMove.item && inventory.getStack(i).damage == toMove.damage) {
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
        for (i in PLAYER_INVENTORY_START downTo if (toMove.item is ArmorItem) HOTBAR_START else PLAYER_INVENTORY_END) {
            val inventoryStack = inventory.getStack(i)
            if (inventoryStack.item == toMove.item && inventoryStack.maxDamage - inventoryStack.damage > minDurability.getValue()) {
                matchingToolSlot = i
                continue
            }
            if (inventoryStack.isEmpty) {
                emptySlot = i
                continue
            }
            if (inventoryStack.maxDamage == 0) {
                nonBreakableSlot = i
                continue
            }
        }
        return matchingToolSlot ?: (emptySlot ?: nonBreakableSlot)
    }

    private fun InGame.swap(fromSlot: Int, toSlot: Int) {
        val hotbarSlot:Int
        val otherSlot:Int
        if (fromSlot in HOTBAR_START..HOTBAR_END) {
            hotbarSlot = fromSlot
            otherSlot = toSlot
        } else {
            hotbarSlot = toSlot
            otherSlot = fromSlot
        }

        val syncId = player.playerScreenHandler?.syncId ?: 0
//        player.playerScreenHandler.onSlotClick(getSlot(otherSlot)!!, hotbarSlot, SlotActionType.SWAP, player)
        interactionManager.clickSlot(syncId, getSlot(otherSlot)!!, hotbarSlot, SlotActionType.SWAP, player)
    }

    private fun InGame.getSlot(inventoryIndex:Int) : Int? {
        for (slot in player.playerScreenHandler?.slots ?: listOf()) {
            if (slot.index == inventoryIndex) {
                return slot.id
            }
        }
        return null
    }

    override fun createMeteorImplementation(): meteordevelopment.meteorclient.systems.modules.Module {
        return EquipmentSaverMeteorImplementation(this)
    }

    class EquipmentSaverMeteorImplementation(module: Module) : MeteorModule(module)
}