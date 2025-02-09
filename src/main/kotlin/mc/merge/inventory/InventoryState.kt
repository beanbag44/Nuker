@file:Suppress("unused")

package mc.merge.inventory

import mc.merge.ModCore.mc
import mc.merge.util.runInGame
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
//? if >=1.21.2
/*import net.minecraft.recipe.RecipeDisplayEntry*/
//? if >=1.20.5
/*import net.minecraft.registry.entry.RegistryEntry*/
import net.minecraft.screen.*
import net.minecraft.screen.slot.*
import net.minecraft.village.TradeOffer


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
        fun get(): InventoryState = runInGame{fromScreenHandler(player.currentScreenHandler)}!!

        private fun fromScreenHandler(handler: ScreenHandler): InventoryState {

            return when (handler) {
                is AnvilScreenHandler               -> AnvilScreenState(handler)
                is BeaconScreenHandler              -> BeaconScreenState(handler)
                is BlastFurnaceScreenHandler        -> BlastFurnaceScreenState(handler)
                is BrewingStandScreenHandler        -> BrewingStandScreenState(handler)
                is CartographyTableScreenHandler    -> CartographyTableScreenState(handler)
                is CrafterScreenHandler             -> CrafterScreenState(handler)
                is CraftingScreenHandler            -> CraftingTableScreenState(handler)
                is EnchantmentScreenHandler         -> EnchantingTableScreenState(handler)
                is FurnaceScreenHandler             -> FurnaceScreenState(handler)
                is Generic3x3ContainerScreenHandler -> Generic3x3ScreenState(handler)
                is GenericContainerScreenHandler    -> GenericContainerScreenState(handler)
                is GrindstoneScreenHandler          -> GrindstoneScreenState(handler)
                is HopperScreenHandler              -> HopperScreenState(handler)
                is HorseScreenHandler               -> HorseScreenState(handler)
                is LecternScreenHandler             -> InventoryState(handler) //No meaningful state
                is LoomScreenHandler                -> LoomScreenState(handler)
                is MerchantScreenHandler            -> MerchantScreenState(handler)
                is PlayerScreenHandler              -> PlayerInventoryScreenState(handler)
                is ShulkerBoxScreenHandler          -> ShulkerBoxScreenState(handler)
                is SmithingScreenHandler            -> SmithingScreenState(handler)
                is SmokerScreenHandler              -> SmokerScreenState(handler)
                is StonecutterScreenHandler         -> StonecutterScreenState(handler)
                else -> InventoryState(handler)
            }
        }
    }
}

class AnvilScreenState(
    val inputSlot1: Slot,
    val inputSlot2: Slot,
    val outputSlot: Slot
) : InventoryState() {
    constructor(handler: AnvilScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2),
    )
}

class BeaconScreenState(
    val paymentSlot: Slot,
    //? if <1.20.5 {
    val primaryEffect: StatusEffect?,
    val secondaryEffect: StatusEffect?,
    //?} else {
    /*val primaryEffect: RegistryEntry<StatusEffect>?,
    val secondaryEffect: RegistryEntry<StatusEffect>?,
    *///?}
) : InventoryState() {
    constructor(handler: BeaconScreenHandler) : this(
        handler.getSlot(0),
        handler.primaryEffect,
        handler.secondaryEffect,
    )
}

class BlastFurnaceScreenState(
    val inputSlot: Slot,
    val fuelSlot: FurnaceFuelSlot,
    val outputSlot: FurnaceOutputSlot
) : InventoryState() {
    constructor(handler: BlastFurnaceScreenHandler) : this(
        handler.getSlot(0),
        handler.slots.filterIsInstance<FurnaceFuelSlot>().first(),
        handler.slots.filterIsInstance<FurnaceOutputSlot>().first(),
    )
}

class BrewingStandScreenState(
    val ingredientSlot: Slot,
    val potionSlots: List<Slot>,
    val fuelSlot: Slot
) : InventoryState() {
    constructor(handler: BrewingStandScreenHandler) : this(
        handler.getSlot(3),
        handler.slots.filter { it.id in 0..2 },
        handler.getSlot(4),
    )
}

class CartographyTableScreenState(
    val inputSlot1: Slot,
    val inputSlot2: Slot,
    val outputSlot: Slot
) : InventoryState() {
    constructor(handler: CartographyTableScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2),
    )
}

class CrafterScreenState(
    val inputSlots: List<CrafterInputSlot>,
    val outputSlot: CrafterOutputSlot
) : InventoryState() {
    constructor(handler: CrafterScreenHandler) : this(
        handler.slots.filterIsInstance<CrafterInputSlot>(),
        handler.slots.filterIsInstance<CrafterOutputSlot>().first(),
    )
}

class CraftingTableScreenState(
    val inputSlots: List<Slot>,
    val outputSlot: CraftingResultSlot,
    //? if <1.21.2 {
    val recipes: List<Recipe<*>>?,
    //?} else {
    /*val recipes: List<RecipeDisplayEntry>?,
    *///?}
) : InventoryState() {
    constructor(handler: CraftingScreenHandler) : this(
        handler.slots.filter { it.id in 1..9 },
        handler.slots.filterIsInstance<CraftingResultSlot>().first(),
        //? if <1.21.2 {
        mc.player?.recipeBook?.orderedResults?.flatMap{ it.allRecipes }?.map { it.value }
        //?} else {
        /*mc.player?.recipeBook?.orderedResults?.flatMap{ it.allRecipes }
    *///?}
    )
}

class EnchantingTableScreenState(
    val inputSlot: Slot,
    val lapisSlot: Slot,
) : InventoryState() {
    constructor(handler: EnchantmentScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
    )
}

class FurnaceScreenState(
    val inputSlot: Slot,
    val fuelSlot: FurnaceFuelSlot,
    val outputSlot: FurnaceOutputSlot
) : InventoryState() {
    constructor(handler: FurnaceScreenHandler) : this(
        handler.getSlot(0),
        handler.slots.filterIsInstance<FurnaceFuelSlot>().first(),
        handler.slots.filterIsInstance<FurnaceOutputSlot>().first(),
    )
}

class Generic3x3ScreenState(
    val containerSlots: List<Slot>
) : InventoryState() {
    constructor(handler: Generic3x3ContainerScreenHandler) : this(
        handler.slots.filter { it.id < 9 },
    )
}

class GenericContainerScreenState(
    val containerSlots: List<Slot>,
) : InventoryState() {
    constructor(handler: GenericContainerScreenHandler) : this(
        handler.slots.filter { it.id < handler.rows * 9 },
    )
}

class GrindstoneScreenState(
    val inputSlot: Slot,
    val additionalSlot: Slot,
    val outputSlot: Slot
) : InventoryState() {
    constructor(handler: GrindstoneScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2),
    )
}

class HopperScreenState(
    val hopperSlots: List<Slot>,
) : InventoryState() {
    constructor(handler: HopperScreenHandler) : this(
        handler.slots.filter { it.id < 5 },
    )
}

class HorseScreenState(
    val saddleSlot: Slot,
    val armorSlot: Slot,
    val chestSlots: List<Slot>,
) : InventoryState() {
    constructor(handler: HorseScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.slots.filter { it.inventory !is PlayerInventory },
    )
}
class LoomScreenState(
    val bannerSlot: Slot,
    val dyeSlot: Slot,
    val patternSlot: Slot,
    val resultSlot: Slot
) : InventoryState() {
    constructor(handler: LoomScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2),
        handler.getSlot(3),
    )
}

class MerchantScreenState(
    val inputSlot1: Slot,
    val inputSlot2: Slot,
    val outputSlot: TradeOutputSlot,
    val trades: List<TradeOffer>,
) : InventoryState() {
    constructor(handler: MerchantScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2) as TradeOutputSlot,
        handler.recipes
    )
}

class PlayerInventoryScreenState(
    val offhandSlot: Slot,
    val armorSlots: List<Slot>,
    val craftingTableSlots: List<Slot>,
    val craftingTableOutput: CraftingResultSlot,
    //? if <1.21.2 {
    val recipes: List<Recipe<*>>?,
    //?} else {
    /*val recipes: List<RecipeDisplayEntry>?,
    *///?}
) : InventoryState() {
    constructor(handler: PlayerScreenHandler) : this(
        handler.getSlot(45),
        handler.slots.filter { it.id in 36..39 },
        handler.slots.filter { it.id in 1..9 },
        handler.getSlot(0) as CraftingResultSlot,
        //? if <1.21.2 {
        mc.player?.recipeBook?.orderedResults?.flatMap{ it.allRecipes }?.map { it.value }
        //?} else {
        /*mc.player?.recipeBook?.orderedResults?.flatMap{ it.allRecipes }
        *///?}
    )
}

class ShulkerBoxScreenState(
    val shulkerBoxSlots: List<ShulkerBoxSlot>,
) : InventoryState() {
    constructor(handler: ShulkerBoxScreenHandler) : this(
        handler.slots.filterIsInstance<ShulkerBoxSlot>(),
    )
}

class SmithingScreenState(
    val templateSlot: Slot,
    val equipmentSlot: Slot,
    val materialSlot: Slot
) : InventoryState() {
    constructor(handler: SmithingScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
        handler.getSlot(2),
    )
}

class SmokerScreenState(
    val inputSlot: Slot,
    val fuelSlot: FurnaceFuelSlot,
    val outputSlot: FurnaceOutputSlot
) : InventoryState() {
    constructor(handler: SmokerScreenHandler) : this(
        handler.getSlot(0),
        handler.slots.filterIsInstance<FurnaceFuelSlot>().first(),
        handler.slots.filterIsInstance<FurnaceOutputSlot>().first(),
    )
}

class StonecutterScreenState(
    val inputSlot: Slot,
    val outputSlot: Slot
) : InventoryState() {
    constructor(handler: StonecutterScreenHandler) : this(
        handler.getSlot(0),
        handler.getSlot(1),
    )
}