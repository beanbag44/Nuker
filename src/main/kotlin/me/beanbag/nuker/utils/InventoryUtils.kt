package me.beanbag.nuker.utils

import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.registry.tag.FluidTags
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
        networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
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

    /** ticksToBreakBlock = roundup(1 / calcBreakDelta(...))*/
    fun InGame.percentDamagePerTick(state: BlockState, pos: BlockPos, toolSlot: Int): Float {
        //https://minecraft.fandom.com/wiki/Breaking#Calculation
        val blockHardness = state.getHardness(world, pos)
        if (blockHardness == -1.0f) {
            return 0.0f
        } else {
            var damage = getBlockBreakingSpeed(state, toolSlot) / blockHardness
            damage /= if (!state.isToolRequired || player.inventory.getStack(toolSlot).isSuitableFor(state)) {
                30
            } else {
                100
            }
            return damage
        }
    }

    private fun InGame.getBlockBreakingSpeed(state: BlockState, toolSlot: Int): Float {
        val noToolSpeed = 1.0f
        val baseEfficiencyIncrease = 1
        val hasteLevelIncrease = 0.2f
        val hasteIncreaseExploit = 1
        val waterModifier = 0.2f
        val inAirModifier = 0.2f

        var breakingSpeed = 1.0f
        //tool
        val toolSpeed = player.inventory.getStack(toolSlot).getMiningSpeedMultiplier(state)
        if (toolSpeed != noToolSpeed) {
            breakingSpeed *= toolSpeed
            val itemStack: ItemStack = player.inventory.getStack(toolSlot)
            val efficiencyLevel = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack)
            if (efficiencyLevel > 0 && !itemStack.isEmpty) {
                breakingSpeed += (efficiencyLevel * efficiencyLevel + baseEfficiencyIncrease).toFloat()
            }
        }
        //haste
        if (StatusEffectUtil.hasHaste(player)) {
            breakingSpeed += breakingSpeed * (StatusEffectUtil.getHasteAmplifier(player) + hasteIncreaseExploit).toFloat() * hasteLevelIncrease
        }
        //mining fatigue
        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            breakingSpeed *= when (player.getStatusEffect(StatusEffects.MINING_FATIGUE)?.amplifier) {
                0 -> 0.3f
                1 -> 0.09f
                2 -> 0.0027f
                3 -> 8.1E-4f
                else -> 8.1E-4f
            }
        }
        //water
        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            breakingSpeed *= waterModifier
        }
        //in air
        if (!player.isOnGround) {
            breakingSpeed *= inAirModifier
        }

        return breakingSpeed
    }
}