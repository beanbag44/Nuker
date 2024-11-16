package me.beanbag.nuker.utils

import me.beanbag.nuker.ModConfigs.inventoryHandler
import me.beanbag.nuker.utils.BlockUtils.getBlockBreakingSpeed
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.math.BlockPos

object InventoryUtils {
    const val HOTBAR_SIZE = 9

    fun InGame.swapTo(item: Item): Boolean {
        val hotbarSlot = getInHotbar(item)
        if (hotbarSlot == -1) return false
        swapTo(hotbarSlot)
        return true
    }

    fun InGame.swapTo(slot: Int): Boolean {
        if (player.inventory?.selectedSlot == slot
            || slot !in 0..8) {
            return false
        }

        player.inventory?.selectedSlot = slot
        inventoryHandler.isSendingPacket = true
        networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        inventoryHandler.isSendingPacket = true
        return true
    }

    fun InGame.getBestTool(state: BlockState, pos: BlockPos): Int {
        val selectedSlot = player.inventory.selectedSlot
        var bestTool = selectedSlot
        var bestTimeToMine = percentDamagePerTick(state, pos, selectedSlot)
        for (i in 0..8) {
            if (i == selectedSlot) continue
            val currentToolsTimeToMine = percentDamagePerTick(state, pos, i)
            if (currentToolsTimeToMine > bestTimeToMine) {
                bestTimeToMine = currentToolsTimeToMine
                bestTool = i
            }
        }
        return bestTool
    }

    fun InGame.getInHotbar(item: Item): Int {
        for (i in 0..8) {
            if (player.inventory.getStack(i).item == item) {
                return i
            }
            continue
        }
        return -1
    }

    /** `ticksToBreakBlock = ceil(1 / percentDamagePerTick(...))`
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Breaking#Calculation">Breaking Calculation</a>
    */
    fun InGame.percentDamagePerTick(state: BlockState, pos: BlockPos, toolSlot: Int): Float {
        val blockHardness = state.getHardness(world, pos)
        if (blockHardness == -1.0f) {
            return 0.0f
        } else {
            var damage = getBlockBreakingSpeed(state, player.inventory.getStack(toolSlot)) / blockHardness
            damage /= if (!state.isToolRequired || player.inventory.getStack(toolSlot).isSuitableFor(state)) {
                30
            } else {
                100
            }
            return damage
        }
    }

}