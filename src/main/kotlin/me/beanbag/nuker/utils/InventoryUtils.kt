package me.beanbag.nuker.utils

import me.beanbag.nuker.ModConfigs.mc
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.effect.StatusEffectUtil
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.registry.tag.FluidTags
import net.minecraft.util.math.BlockPos

object InventoryUtils {
    fun swapTo(slot: Int): Boolean {
        if (mc.player?.inventory?.selectedSlot == slot
            || slot !in 0..8) {
            return false
        }

        mc.player?.inventory?.selectedSlot = slot
        mc.networkHandler?.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        return true
    }

    fun getBestTool(state: BlockState, pos: BlockPos): Int {
        mc.player?.let { player ->
            val selectedSlot = player.inventory.selectedSlot
            var bestTool = selectedSlot
            var bestTimeToMine = calcBreakDelta(state, pos, selectedSlot)
            for (i in 0..8) {
                if (i == selectedSlot) continue
                val currentToolsTimeToMine = calcBreakDelta(state, pos, i)
                if (currentToolsTimeToMine > bestTimeToMine) {
                    bestTimeToMine = currentToolsTimeToMine
                    bestTool = i
                }
            }
            return bestTool
        }

        return 0
    }

    fun calcBreakDelta(state: BlockState, pos: BlockPos, toolSlot: Int): Float {
        mc.world?.let { world ->
            mc.player?.let { player ->
                val f: Float = state.getHardness(world, pos)
                if (f == -1.0f) {
                    return 0.0f
                } else {
                    val i = if (!state.isToolRequired
                        || player.inventory.getStack(toolSlot).isSuitableFor(state)) {
                        30
                    } else {
                        100
                    }
                    return getBlockBreakingSpeed(state, toolSlot) / f / i.toFloat()
                }
            }
        }

        return 0f
    }

    private fun getBlockBreakingSpeed(state: BlockState, toolSlot: Int): Float {
        mc.player?.let { player ->
            var f: Float = player.inventory.getStack(toolSlot).getMiningSpeedMultiplier(state)
            if (f > 1.0f) {
                val itemStack: ItemStack = player.inventory.getStack(toolSlot)
                val i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack)
                if (i > 0 && !itemStack.isEmpty) {
                    f += (i * i + 1).toFloat()
                }
            }

            if (StatusEffectUtil.hasHaste(player)) {
                f *= 1.0f + (StatusEffectUtil.getHasteAmplifier(player) + 1).toFloat() * 0.2f
            }

            if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                val g = when (player.getStatusEffect(StatusEffects.MINING_FATIGUE)?.amplifier) {
                    0 -> 0.3f
                    1 -> 0.09f
                    2 -> 0.0027f
                    3 -> 8.1E-4f
                    else -> 8.1E-4f
                }
                f *= g
            }

            if (player.isSubmergedIn(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(player)
            ) {
                f /= 5.0f
            }

            if (!player.isOnGround) {
                f /= 5.0f
            }

            return f
        }

        return 0f
    }
}