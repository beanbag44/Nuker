package me.beanbag.nuker.inventory

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.utils.runInGame
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.*
import net.minecraft.screen.slot.Slot


/**
 * Represents the current inventory for the current screen (ex: crafting screen, chest screen, etc).
 */
open class InventoryState(
    val slots: List<Slot> = mc.player?.currentScreenHandler?.slots ?: emptyList(),
    val hotbarSlots: List<Slot> = slots.filter { it.id < 9 && it.inventory is PlayerInventory },
    val inventorySlots: List<Slot> = slots.filter { it.id in 9..35  && it.inventory is PlayerInventory },
    val playerHand: ItemStack? = mc.player?.mainHandStack,
    val offhand: ItemStack? = mc.player?.offHandStack,
) {
    constructor(handler: ScreenHandler) : this(
        handler.slots,
        handler.slots.filter { it.id < 9 && it.inventory is PlayerInventory },
        handler.slots.filter { it.id in 9..35  && it.inventory is PlayerInventory },
        handler.getSlot(40).stack,
        handler.getSlot(45).stack
    )
    companion object {
        fun get():InventoryState = runInGame{fromScreenHandler(player.currentScreenHandler)}!!

        private fun fromScreenHandler(handler: ScreenHandler): InventoryState {
            return when (handler) {
                is AnvilScreenHandler               -> InventoryState(handler)
                is BeaconScreenHandler              -> InventoryState(handler)
                is BlastFurnaceScreenHandler        -> InventoryState(handler)
                is BrewingStandScreenHandler        -> InventoryState(handler)
                is CartographyTableScreenHandler    -> InventoryState(handler)
                is CrafterScreenHandler             -> InventoryState(handler)
                is CraftingScreenHandler            -> InventoryState(handler)
                is EnchantmentScreenHandler         -> InventoryState(handler)
                is FurnaceScreenHandler             -> InventoryState(handler)
                is Generic3x3ContainerScreenHandler -> InventoryState(handler)
                is GenericContainerScreenHandler    -> InventoryState(handler)
                is GrindstoneScreenHandler          -> InventoryState(handler)
                is HopperScreenHandler              -> InventoryState(handler)
                is HorseScreenHandler               -> InventoryState(handler)
                is LecternScreenHandler             -> InventoryState(handler)
                is LoomScreenHandler                -> InventoryState(handler)
                is MerchantScreenHandler            -> InventoryState(handler)
                is PlayerScreenHandler              -> PlayerInventoryState(handler)
                is ShulkerBoxScreenHandler          -> InventoryState(handler)
                is SmithingScreenHandler            -> InventoryState(handler)
                is SmokerScreenHandler              -> InventoryState(handler)
                is StonecutterScreenHandler         -> InventoryState(handler)
                else -> InventoryState(handler)
            }
        }
    }
}

class PlayerInventoryState(
    val offhandSlot: Slot,
    val armorSlots: List<Slot>,
    val craftingTableSlots: List<Slot>,
    val craftingTableOutput: Slot
) : InventoryState() {
    constructor(handler: PlayerScreenHandler) : this(
        handler.getSlot(45),
        handler.slots.filter { it.id in 36..39 },
        handler.slots.filter { it.id in 1..9 },
        handler.getSlot(0)
    )
}