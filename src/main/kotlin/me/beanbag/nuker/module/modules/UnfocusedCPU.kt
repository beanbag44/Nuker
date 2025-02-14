package me.beanbag.nuker.module.modules

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.module.Module

class UnfocusedCPU : Module("Unfocused CPU", "limits frame rate when the game window is not focused"){
    private val generalGroup = group("General", "General settings")
    val fps = setting(generalGroup, "Target FPS",
        "Target FPS to set as the limit when the window is not focused",
        1,
        min = 0,
        max = 100)

    init {
        EventBus.subscribe<RenderEvent.Render3DEvent>(this) {
            mc.window.framerateLimit = if (!mc.isWindowFocused) fps.getValue() else mc.options.maxFps.value
        }
        enabledSetting.getOnChange().add {
            mc.window.framerateLimit = if (it) fps.getValue() else mc.options.maxFps.value
        }
    }
}