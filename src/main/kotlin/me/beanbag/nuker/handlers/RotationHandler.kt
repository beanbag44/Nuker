package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.ModConfigs.rusherIsPresent
import me.beanbag.nuker.eventsystem.EventBus.MIN_PRIORITY
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.RotationHandler.InputDirections.Companion.getCurrentInput
import me.beanbag.nuker.utils.InGame
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.getBoundKeyOf
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.rusherhack.client.api.RusherHackAPI

object RotationHandler: IHandler {
    override var currentlyBeingUsedBy: Module? = null
    override var priority = 0
    private var rotatedThisTick = false
    private var ticksSinceLastRotation = 0
    var freeLooking = false
    var yaw = 0f
    var pitch = 0f

    init {
        onInGameEvent<TickEvent.Pre>(MIN_PRIORITY) {
            if (!rotatedThisTick) {
                ticksSinceLastRotation++
            } else {
                rotatedThisTick = false
            }

            if (!freeLooking) return@onInGameEvent

            if (ticksSinceLastRotation >= 1) {
                cancelRotations()
                return@onInGameEvent
            }

            val currentInputDirection = getCurrentInput()

            val cameraYaw = mc.gameRenderer.camera.yaw % 360

            if (currentInputDirection == InputDirections.NONE) {
                InputDirections.apply(InputDirections.NONE)
                return@onInGameEvent
            }

            val inputYaw: Float = currentInputDirection.yaw
            val playerYaw = (player.yaw + 360) % 360

            val playerRelativeYaw = inputYaw + playerYaw
            val resultPlayerYaw = playerRelativeYaw - cameraYaw
            val resultDirection = InputDirections.getDirection(resultPlayerYaw) ?: return@onInGameEvent

            mc.options.forwardKey.isPressed = false
            mc.options.backKey.isPressed = false
            mc.options.leftKey.isPressed = false
            mc.options.rightKey.isPressed = false

            InputDirections.apply(resultDirection)
        }
    }

    fun InGame.rotate(yaw: Float, pitch: Float, silent: Boolean, useRusherIfPossible: Boolean) {
        if (useRusherIfPossible && rusherIsPresent) {
            RusherHackAPI.getRotationManager().updateRotation(yaw, pitch)
            return
        }

        if (silent && !freeLooking) {
            freeLooking = true
            RotationHandler.yaw = player.yaw
            RotationHandler.pitch = player.pitch
        }

        player.yaw = yaw
        player.pitch = pitch
        rotatedThisTick = true
        ticksSinceLastRotation = 0
    }

    private fun InGame.cancelRotations() {
        player.yaw = yaw
        player.pitch = pitch
        freeLooking = false

        mc.options.forwardKey.isPressed =
            InputUtil.isKeyPressed(mc.window.handle, getBoundKeyOf(mc.options.forwardKey).code)
        mc.options.backKey.isPressed =
            InputUtil.isKeyPressed(mc.window.handle, getBoundKeyOf(mc.options.backKey).code)
        mc.options.leftKey.isPressed =
            InputUtil.isKeyPressed(mc.window.handle, getBoundKeyOf(mc.options.leftKey).code)
        mc.options.rightKey.isPressed =
            InputUtil.isKeyPressed(mc.window.handle, getBoundKeyOf(mc.options.rightKey).code)
    }

    internal enum class InputDirections(val yaw: Float) {
        FORWARD(0f),
        FORWARD_LEFT(45f),
        FORWARD_RIGHT(315f),
        LEFT(90f),
        RIGHT(270f),
        BACK(180f),
        BACK_LEFT(135f),
        BACK_RIGHT(225f),
        NONE(-1f);

        companion object {
            fun getDirection(yaw: Float): InputDirections? {
                var yaw = yaw
                while (yaw < 0) yaw += 360f
                yaw %= 360
                return when {
                    yaw < 22.5 -> FORWARD
                    yaw < 67.5 -> FORWARD_LEFT
                    yaw < 112.5 -> LEFT
                    yaw < 157.5 -> BACK_LEFT
                    yaw < 202.5 -> BACK
                    yaw < 247.5 -> BACK_RIGHT
                    yaw < 292.5 -> RIGHT
                    yaw < 337.5 -> FORWARD_RIGHT
                    else -> null
                }
            }

            fun InGame.getCurrentInput(): InputDirections {
                var x = 0
                var y = 0

                // Adjust x and y based on key inputs
                if (isKeyPressed(mc.options.forwardKey)) x += 1
                if (isKeyPressed(mc.options.backKey)) x -= 1
                if (isKeyPressed(mc.options.rightKey)) y += 1
                if (isKeyPressed(mc.options.leftKey)) y -= 1

                // Determine InputDirections based on x and y
                return when {
                    x > 0 && y > 0 -> FORWARD_RIGHT
                    x > 0 && y < 0 -> FORWARD_LEFT
                    x > 0 -> FORWARD
                    x < 0 && y > 0 -> BACK_RIGHT
                    x < 0 && y < 0 -> BACK_LEFT
                    x < 0 -> BACK
                    y > 0 -> RIGHT
                    y < 0 -> LEFT
                    else -> NONE
                }
            }

            fun apply(direction: InputDirections) {
                when (direction) {
                    FORWARD -> mc.options.forwardKey.isPressed = true
                    FORWARD_LEFT -> {
                        mc.options.leftKey.isPressed = true
                        mc.options.forwardKey.isPressed = true
                    }

                    FORWARD_RIGHT -> {
                        mc.options.rightKey.isPressed = true
                        mc.options.forwardKey.isPressed = true
                    }

                    LEFT -> mc.options.leftKey.isPressed = true
                    RIGHT -> mc.options.rightKey.isPressed = true
                    BACK -> mc.options.backKey.isPressed = true
                    BACK_LEFT -> {
                        mc.options.backKey.isPressed = true
                        mc.options.leftKey.isPressed = true
                    }

                    BACK_RIGHT -> {
                        mc.options.backKey.isPressed = true
                        mc.options.rightKey.isPressed = true
                    }

                    NONE -> {
                        mc.options.forwardKey.isPressed = false
                        mc.options.leftKey.isPressed = false
                        mc.options.rightKey.isPressed = false
                        mc.options.backKey.isPressed = false
                    }
                }
            }
        }
    }

    private fun InGame.isKeyPressed(keyBinding: KeyBinding): Boolean {
        return InputUtil.isKeyPressed(
            mc.window.handle,
            getBoundKeyOf(keyBinding).code
        )
    }
}