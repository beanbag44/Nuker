package me.beanbag.nuker.inventory

import net.minecraft.screen.slot.Slot

interface InventoryAction

class SwapAction(var fromSlot: Slot, var toSlotSlot: Slot) : InventoryAction

class DropAction(var slot: Slot, var all: Boolean = false) : InventoryAction

class UpdateSelectedAction(var slot: Int) : InventoryAction

class MoveAction(var fromSlot: Slot, var toSlot: Slot) : InventoryAction