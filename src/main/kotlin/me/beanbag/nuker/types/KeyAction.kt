package me.beanbag.nuker.types

import org.lwjgl.glfw.GLFW

enum class KeyAction {
    Press,
    Release,
    Repeat;

    companion object {
        fun get(action: Int): KeyAction =
            when (action) {
                GLFW.GLFW_PRESS -> Press
                GLFW.GLFW_RELEASE -> Release
                else -> Repeat
            }
    }
}