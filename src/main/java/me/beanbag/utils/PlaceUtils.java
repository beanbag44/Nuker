package me.beanbag.utils;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import static me.beanbag.Nuker.mc;

public class PlaceUtils {
    public static void place(BlockHitResult blockHitResult) {
        if (mc.player == null
                || mc.getNetworkHandler() == null) return;
        if (mc.player == null
                || mc.getNetworkHandler() == null
                || mc.interactionManager == null) return;

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
    }
    public static int findNetherrack() {
        if (mc.player == null) return -1;
        for (int i = 0; i <9; i++) {
            if (mc.player.getInventory().getStack(i).getItem().equals(Items.NETHERRACK)) {
                return i;
            }
        }
        return -1;
    }
}
