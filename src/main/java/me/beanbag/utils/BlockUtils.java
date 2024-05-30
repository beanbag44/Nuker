package me.beanbag.utils;

import me.beanbag.Nuker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static me.beanbag.Nuker.mc;

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
    public static List<BlockPos> sortBlocks(List<BlockPos> list) {
        List<BlockPos> sortedList = new ArrayList<>();
        if (mc.player == null) return sortedList;
        sortedList.addAll(list.stream()
                .sorted(Comparator.comparingDouble(a -> mc.player.getEyePos().distanceTo(a.toCenterPos()))).toList());
        switch (Nuker.mineSort) {
            case FARTHEST -> Collections.reverse(sortedList);
            case TOP_DOWN -> sortedList.sort(Comparator.comparingDouble((a) -> -a.getY()));
            case BOTTOM_UP -> sortedList.sort(Comparator.comparingDouble(BlockPos::getY));
            case RANDOM -> Collections.shuffle(sortedList);
        }
        return sortedList;
    }
    public static void filterImpossibleBlocks() {
        if (mc.world == null) return;
        Nuker.spherePosList.removeIf(pos -> {
            Block block = mc.world.getBlockState(pos).getBlock();
            return (block.getHardness() == 600
                    || block.getHardness() == -1
                    || block.equals(Blocks.BARRIER)
            );
        });
    }
    public static void filterLiquids() {
        if (mc.world == null) return;
        Nuker.spherePosList.removeIf(pos -> {
            BlockState state = mc.world.getBlockState(pos);
            return (state.getBlock().equals(Blocks.WATER)
                    || state.getFluidState().getFluid().equals(Fluids.FLOWING_WATER)
                    || state.getBlock().equals(Blocks.LAVA)
                    || state.getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)
            );
        });
    }
    public static List<BlockPos> getPlayerBlockSphere(Vec3d playerPos, int r) {
        List<BlockPos> posList = getPlayerBlockCube(playerPos, r);
        posList.removeIf(next -> !isWithinRadius(playerPos, next, r));
        return new ArrayList<>(posList);
    }
    private static List<BlockPos> getPlayerBlockCube(Vec3d playerPos, int r) {
        List<BlockPos> posList = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    posList.add(new BlockPos(
                            (int) Math.floor(playerPos.x + x)
                            , (int) Math.floor(playerPos.y + y)
                            , (int) Math.floor(playerPos.z + z))
                    );
                }
            }
        }
        return posList;
    }
    public static Boolean isWithinRadius(Vec3d playerPos, BlockPos blockPos, int r) {
        return playerPos.distanceTo(blockPos.toCenterPos()) <= r;
    }
}
