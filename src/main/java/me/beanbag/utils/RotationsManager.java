package me.beanbag.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static me.beanbag.Nuker.mc;

public class RotationsManager {
    private static boolean deSynced = false;
    private static boolean rotatedThisTick = false;
    private static int ticksSinceLastRotation = 0;
    public static void initEventHandler() {
        ClientTickEvents.END_CLIENT_TICK.register((mc) -> {
            if (mc.player == null) return;
            if (!rotatedThisTick) {
                ticksSinceLastRotation++;
            } else {
                rotatedThisTick = false;
            }
            if (ticksSinceLastRotation >= 1
                    && deSynced) {
                deSynced = false;
                mc.player.setYaw(FreeLook.INSTANCE.getYaw());
                mc.player.setPitch(FreeLook.INSTANCE.getPitch());
                FreeLook.INSTANCE.disable();
            }
        });
    }
    public static void rotate(int yaw, int pitch) {
        if (mc.player != null
                && mc.getNetworkHandler() != null) {
            onRotate();
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        }
    }
    public static void lookAt(Vec3d vec) {
        if (mc.player != null
                && mc.getNetworkHandler() != null) {
            onRotate();
//            float[] yawPitch = calculateLookAt(mc.player.getEyePos(), vec);
//            mc.player.setYaw(yawPitch[0]);
//            mc.player.setPitch(yawPitch[1]);
            mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, vec);
//            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        }
    }
    public static boolean canSeeBlockFace(PlayerEntity player, BlockPos blockPos, Direction direction) {
        Vec3d playerEyePos = player.getEyePos();
        switch (direction) {
            case UP:
                if (blockPos.getY() + 1 > playerEyePos.getY()) return false;
                break;
            case DOWN:
                if (blockPos.getY() < playerEyePos.getY()) return false;
                break;
            case NORTH:
                if (blockPos.getZ() < playerEyePos.getZ()) return false;
                break;
            case SOUTH:
                if (blockPos.getZ() + 1 > playerEyePos.getZ()) return false;
                break;
            case EAST:
                if (blockPos.getX() + 1 > playerEyePos.getX()) return false;
                break;
            case WEST:
                if (blockPos.getX() < playerEyePos.getX()) return false;
                break;
        }
        return true;
    }
    public static void onRotate() {
        if (!FreeLook.INSTANCE.isEnabled()) {
            FreeLook.INSTANCE.enable();
        }
        deSynced = true;
        rotatedThisTick = true;
        ticksSinceLastRotation = 0;
    }
}
