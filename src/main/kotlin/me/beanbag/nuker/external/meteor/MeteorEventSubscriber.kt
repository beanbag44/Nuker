package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.utils.FileManager
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent
import meteordevelopment.meteorclient.events.render.Render3DEvent
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority

class MeteorEventSubscriber {
    fun subscribe() {
        MeteorClient.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun onRender(event: Render3DEvent) {
        if (!ModConfigs.rusherIsPresent) {
            EventBus.post(RenderEvent.Render3DEvent(MeteorRenderer3D(event.renderer)))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onSetKeybind(event: ModuleBindChangedEvent) {
        val meteorModule = event.module

        for (module in ModConfigs.modules.values) {
            if (module.name != meteorModule.name) continue

            with(meteorModule.keybind) {
                val modifiersField = this::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                module.keybind.set(value, modifiersField.get(this) as Int, isKey)
            }

            break
        }

        FileManager.saveModuleConfigs()
    }
}