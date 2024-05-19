package me.beanbag.utils;

import lombok.Getter;
import me.beanbag.mixin.MixinAccessorKeyBinding;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.beanbag.Nuker.mc;

public class MovementHandler {
    private static boolean disableNextTick = true;
    public static void initEventHandler() {
        ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
            if (mc.player == null) return;

            if (!FreeLook.INSTANCE.isEnabled()) {
                if (!disableNextTick) {
                    return;
                }
            }

            // Get current inputs
            InputDirections currentInputDirection = InputDirections.getCurrentInput();

            float cameraYaw = mc.gameRenderer.getCamera().getYaw() % 360;

            if (currentInputDirection != InputDirections.NONE) {
                // lastPlayerInputDirection = currentInputDirection;
                // Calculate resulting control direction and apply them
                float inputYaw = currentInputDirection.getYaw();
                float playerYaw = (mc.player.getYaw() + 360) % 360;

                // Calculate the result yaw
                float playerRelativeYaw = inputYaw + playerYaw;
                float resultPlayerYaw = playerRelativeYaw - cameraYaw;
                InputDirections resultDirection = InputDirections.getDirection(resultPlayerYaw);
                if (resultDirection == null) {
                    return;
                }

                mc.options.forwardKey.setPressed(false);
                mc.options.backKey.setPressed(false);
                mc.options.leftKey.setPressed(false);
                mc.options.rightKey.setPressed(false);

                InputDirections.apply(resultDirection);
            } else {
                InputDirections.apply(InputDirections.NONE);
            }
            if (!FreeLook.INSTANCE.isEnabled()) {
                if (disableNextTick) {
                    disableNextTick = false;
                    InputDirections.apply(InputDirections.getCurrentInput());
                }
            }
        });
    }

    public static void disable() {
        disableNextTick = true;
    }

    @Getter
    enum InputDirections {
        FORWARD(0),
        FORWARD_LEFT(45),
        FORWARD_RIGHT(315),
        LEFT(90),
        RIGHT(270),
        BACK(180),
        BACK_LEFT(135),
        BACK_RIGHT(225),
        NONE(-1);
        private final float yaw;
        InputDirections(float i) {
            this.yaw = i;
        }

        private static InputDirections getDirection(float yaw) {
            yaw = yaw % 360;
            if (yaw < 0) yaw += 360;
            if (yaw >= 0 && yaw < 22.5) return FORWARD;
            if (yaw >= 22.5 && yaw < 67.5) return FORWARD_LEFT;
            if (yaw >= 67.5 && yaw < 112.5) return LEFT;
            if (yaw >= 112.5 && yaw < 157.5) return BACK_LEFT;
            if (yaw >= 157.5 && yaw < 202.5) return BACK;
            if (yaw >= 202.5 && yaw < 247.5) return BACK_RIGHT;
            if (yaw >= 247.5 && yaw < 292.5) return RIGHT;
            if (yaw >= 292.5 && yaw < 337.5) return FORWARD_RIGHT;
            if (yaw >= 337.5 && yaw < 360) return FORWARD;
            return NONE;
        }

        private static InputDirections getCurrentInput() {
            if (isKeyPressed(mc.options.forwardKey)) {
                if (isKeyPressed(mc.options.leftKey)) {
                    return FORWARD_LEFT;
                } else if (isKeyPressed(mc.options.rightKey)) {
                    return FORWARD_RIGHT;
                } else {
                    return FORWARD;
                }
            } else if (isKeyPressed(mc.options.backKey)) {
                if (isKeyPressed(mc.options.leftKey)) {
                    return BACK_LEFT;
                } else if (isKeyPressed(mc.options.rightKey)) {
                    return BACK_RIGHT;
                } else {
                    return BACK;
                }
            } else if (isKeyPressed(mc.options.leftKey)) {
                return LEFT;
            } else if (isKeyPressed(mc.options.rightKey)) {
                return RIGHT;
            } else {
                return NONE;
            }
        }

        private static void apply(InputDirections direction) {
            switch (direction) {
                case FORWARD -> mc.options.forwardKey.setPressed(true);
                case FORWARD_LEFT -> {
                    mc.options.leftKey.setPressed(true);
                    mc.options.forwardKey.setPressed(true);
                }
                case FORWARD_RIGHT -> {
                    mc.options.rightKey.setPressed(true);
                    mc.options.forwardKey.setPressed(true);
                }
                case LEFT -> mc.options.leftKey.setPressed(true);
                case RIGHT -> mc.options.rightKey.setPressed(true);
                case BACK -> mc.options.backKey.setPressed(true);
                case BACK_LEFT -> {
                    mc.options.backKey.setPressed(true);
                    mc.options.leftKey.setPressed(true);
                }
                case BACK_RIGHT -> {
                    mc.options.backKey.setPressed(true);
                    mc.options.rightKey.setPressed(true);
                }
                case NONE -> {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                }
            }
        }
    }
    private static boolean isKeyPressed(KeyBinding keyBinding) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((MixinAccessorKeyBinding) keyBinding).getBoundKey().getCode());
    }
}