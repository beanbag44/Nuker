package me.beanbag.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static me.beanbag.Nuker.mc;

public class PlaceUtils {
    public static void place(BlockHitResult blockHitResult, boolean packetPlace) {
        if (mc.player == null
                || mc.getCameraEntity() == null
                || mc.interactionManager == null
                || mc.getNetworkHandler() == null) {
            return;
        }

        if (packetPlace) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
        } else {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
        }
    }
    public static int findSuitableBlock() {
        if (mc.player == null) return -1;
        for (int i = 0; i <9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item instanceof BlockItem) {
                if (isSuitableBlock(item)) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static int findObsidian() {
        if (mc.player == null) return -1;
        for (int i = 0; i <9; i++) {
            if (mc.player.getInventory().getStack(i).getItem().equals(Items.OBSIDIAN)) {
                return i;
            }
        }
        return -1;
    }
    public static boolean isSuitableBlock(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        }
        Block block = ((BlockItem) item).getBlock();
        return !(block instanceof FallingBlock)
                && !block.equals(Blocks.OBSIDIAN)
                && block.getDefaultState().isFullCube(mc.world, new BlockPos(0, 321, 0)
        );
    }
    public static List<BlockPos> getBlocksPlayerOccupied() {
        if (mc.player == null) return new ArrayList<>();

        ArrayList<BlockPos> positions = new ArrayList<>();
        BlockPos playerPos = mc.player.getBlockPos();
        int blocksHeightOccupied = (int) Math.ceil(mc.player.getPos().y + mc.player.getHeight() - playerPos.getY());

        positions.add(mc.player.getBlockPos());
        positions.add(mc.player.getBlockPos().up());
        if (blocksHeightOccupied > 2) {
            positions.add(playerPos.up(2));
        }
        if (Math.floor(mc.player.getPos().x + mc.player.getWidth() / 2) > playerPos.getX()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.up(i).east());
            }
        }
        if ((mc.player.getPos().x - mc.player.getWidth() / 2) < playerPos.getX()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.up(i).west());
            }
        }
        if (Math.floor(mc.player.getPos().z + mc.player.getWidth() / 2) > playerPos.getZ()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.up(i).south());
            }
        }
        if ((mc.player.getPos().z - mc.player.getWidth() / 2) < playerPos.getZ()) {
            for (int i = 0; i < blocksHeightOccupied; i++) {
                positions.add(playerPos.up(i).north());
            }
        }
        return positions.stream().distinct().toList();
    }
}
