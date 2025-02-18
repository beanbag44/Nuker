package mc.merge.inventory

import mc.merge.event.events.TickEvent
import mc.merge.event.events.UseBlockEvent
import mc.merge.event.onInGameEvent
import net.minecraft.block.EnderChestBlock
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

class EChestMemory {
    var hasMemory = false
    val memory: DefaultedList<ItemStack> = DefaultedList.ofSize(27, ItemStack.EMPTY)
    var state: EChestState = EChestState.Closed

    init {
        onInGameEvent<UseBlockEvent> {
            if (world.getBlockState(it.hitResult.blockPos).block is EnderChestBlock) {
                state = EChestState.Opening
            }
        }
        onInGameEvent<TickEvent.Pre> {
            if (state == EChestState.Closed) return@onInGameEvent
            if (mc.currentScreen !is GenericContainerScreen) {
                if (state == EChestState.Open) {
                    state = EChestState.Closed
                }
                return@onInGameEvent
            } else {
                state = EChestState.Open
                val inventoryState = InventoryState.get()
                if (inventoryState is GenericContainerScreenState) {
                    val containerSlots = inventoryState.containerSlots

                    val containerFullSlots = containerSlots.count { !it.stack.isEmpty }
                    val memoryFullSlots = memory.count { !it.isEmpty }
                    if (containerFullSlots != memoryFullSlots) {
//                        ChatHandler.sendChatLine("Current Filled: $containerFullSlots\n Old Filled: $memoryFullSlots")
                    }
                    for (i in 0..26) {
                        if (memory[i] != containerSlots[i].stack) {
                            memory[i] = containerSlots[i].stack
                        }
                    }
                    hasMemory = true
                }
            }
        }
    }

}

enum class EChestState {
    Opening,
    Open,
    Closed,
}