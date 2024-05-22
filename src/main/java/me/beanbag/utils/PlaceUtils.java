package me.beanbag.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import static me.beanbag.Nuker.mc;

public class PlaceUtils {
    public static void place(BlockHitResult blockHitResult) {
        if (mc.player == null
                || mc.getCameraEntity() == null
                || mc.interactionManager == null
                || mc.getNetworkHandler() == null) {
            return;
        }

//        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
    }
    public static int findSuitableBlock() {
        if (mc.player == null) return -1;
        for (int i = 0; i <9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                if (!(block instanceof FallingBlock
                        && !block.equals(Blocks.OBSIDIAN))
                        && block.getDefaultState().isFullCube(mc.world, new BlockPos(0, 321, 0))) {
                    return i;
                }
            }
        }
        return -1;
    }
}
