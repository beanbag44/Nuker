package me.beanbag.utils;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtils {
    public static float getBlockBreakingSpeed(BlockState block, PlayerEntity player, ItemStack itemStack) {
        float f = itemStack.getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g = switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= g;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }
    public static double getBlockBreakingTimeMS(ItemStack itemStack, BlockPos pos, PlayerEntity player, World world) {
        BlockState state = world.getBlockState(pos);
        float f = state.getHardness(world, pos);
        float delta;
        if (f == -1.0F) {
            delta = 0.0f;
        } else {
            int i = canHarvest(state, itemStack) ? 30 : 100;
            delta = getBlockBreakingSpeed(state, player, itemStack) / f / (float)i;
        }
        double ticks = 1 / delta;
        double seconds = ticks / 20;
        return seconds * 1000;
    }
    public static boolean canHarvest(BlockState state, ItemStack itemStack) {
        return !state.isToolRequired() || itemStack.isSuitableFor(state);
    }
}
