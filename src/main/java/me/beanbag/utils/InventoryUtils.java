package me.beanbag.utils;

import net.minecraft.util.math.BlockPos;

import static me.beanbag.Nuker.mc;

public class InventoryUtils {
    public static int getBestToolSlot(BlockPos blockPos) {
        if (mc.player == null || mc.world == null) return -1;
        int bestTool = mc.player.getInventory().selectedSlot;
        for (int slotNum = 0; slotNum < 9; slotNum++) {
            if (BlockUtils.getBlockBreakingTimeMS(mc.player.getInventory().getStack(slotNum)
                    , blockPos
                    , mc.player
                    , mc.world) < BlockUtils.getBlockBreakingTimeMS(mc.player.getInventory().getStack(bestTool)
                    , blockPos
                    , mc.player
                    , mc.world)) {
                bestTool = slotNum;
            }
        }
        return bestTool;
    }
}
