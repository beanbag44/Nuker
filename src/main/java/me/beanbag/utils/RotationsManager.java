package me.beanbag.utils;

import me.beanbag.Nuker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.rusherhack.client.api.RusherHackAPI;

import static me.beanbag.Nuker.mc;

public class RotationsManager {
    private static boolean deSynced = false;
    private static boolean rotatedThisTick = false;
    private static int ticksSinceLastRotation = 0;
    public static boolean isInitialized = false;
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
        isInitialized = true;
    }
    public static void rotate(int yaw, int pitch) {
        if (mc.player != null
                && mc.getNetworkHandler() != null) {
            if (Nuker.rusherhackLoaded) {
                RusherHackAPI.getRotationManager().updateRotation(yaw, pitch);
            } else {
                onRotate();
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
            }
        }
    }
    public static void lookAt(Vec3d vec) {
        if (mc.player != null
                && mc.getNetworkHandler() != null) {
            if (Nuker.rusherhackLoaded) {
                float[] yawPitch = calculateLookAt(mc.player.getEyePos(), vec);
                RusherHackAPI.getRotationManager().updateRotation(yawPitch[0], yawPitch[1]);
            } else {
                onRotate();
                mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, vec);
            }
        }
    }
    public static float[] calculateLookAt(Vec3d eyePos, Vec3d targetPos) {
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;

        double yaw = Math.atan2(dz, dx);
        yaw = Math.toDegrees(yaw) - 90;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double pitch = -Math.atan2(dy, horizontalDistance);
        pitch = Math.toDegrees(pitch);

        return new float[]{(float) yaw, (float) pitch};
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
        if (!RotationsManager.isInitialized) {
            RotationsManager.initEventHandler();
            RotationsManager.isInitialized = true;
        }
        if (!MovementHandler.isInitialized) {
            MovementHandler.initEventHandler();
            MovementHandler.isInitialized = true;
        }

        if (!FreeLook.INSTANCE.isEnabled()) {
            FreeLook.INSTANCE.enable();
        }
        deSynced = true;
        rotatedThisTick = true;
        ticksSinceLastRotation = 0;
    }
}
