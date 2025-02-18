package mc.merge.task

import mc.merge.ModCore.inventoryHandler
import mc.merge.event.events.TickEvent
import mc.merge.event.onInGameEvent
import mc.merge.inventory.SelectHotbarSlotAction
import mc.merge.ModCore.mc
import mc.merge.handler.IHandlerController
import net.minecraft.item.ItemStack

class SelectHotbarItem(val condition: (index:Int, stack:ItemStack) -> Boolean, parent: IHandlerController) : Task(parent) {
    var previouslySelected: Int = mc.player?.inventory?.selectedSlot ?: 0
    override fun run() {
        super.run()
        //TODO: tell the inventory handler we are trying to be in control
        previouslySelected = mc.player?.inventory?.selectedSlot ?: previouslySelected
        onInGameEvent<TickEvent.Pre> {
            for (slotIndex in 0..8) {

                val slotStack = player.inventory.main[slotIndex]

                if (condition(slotIndex, slotStack)) {
                    inventoryHandler.selectSlot(this@SelectHotbarItem, SelectHotbarSlotAction(slotIndex))
                    return@onInGameEvent
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        inventoryHandler.releaseSlot(this)
    }
}