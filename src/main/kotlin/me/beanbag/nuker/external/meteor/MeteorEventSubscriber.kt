package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.RenderEvent
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.meteor.KeyEvent
import meteordevelopment.meteorclient.events.render.Render3DEvent
import meteordevelopment.meteorclient.systems.modules.Modules
import meteordevelopment.meteorclient.utils.misc.input.Input
import meteordevelopment.meteorclient.utils.misc.input.KeyAction
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import org.lwjgl.glfw.GLFW

class MeteorEventSubscriber {
    fun subscribe() {
        MeteorClient.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun onRender(event: Render3DEvent) {
//        if (!ModConfigs.rusherIsPresent) { //TODO add this back in when we can hook into rusher's render event
            EventBus.post(RenderEvent(MeteorRenderer(event.renderer)))
//        }
    }

    // hacky workaround to get key binds to work because meteor uses a Map<Class, Module> to trigger key binds, and we only use a single class type
    @EventHandler(priority = EventPriority.HIGH)
    private fun onKey(event: KeyEvent) {
//        if (event.action == KeyAction.Repeat) return
//        if (MeteorClient.mc.currentScreen != null || Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return
//        //Meteor only catches one of the modules and ignores the rest because they are all the same class
//        val okModules = Modules.get().all.filter { module -> module.javaClass == MeteorModule::class.java }
//        val hiddenModules = Modules.get().list.filter { module -> module.javaClass == MeteorModule::class.java && okModules.none { it == module } }
//        //Aka, the same functionality as meteor's meteordevelopment.meteorclient.systems.modules.Modules.onAction
//        hiddenModules.forEach { module ->
//            if (module.keybind.matches(true, event.key, event.modifiers) && (event.action == KeyAction.Press || module.toggleOnBindRelease)) {
//                module.toggle()
//                module.sendToggledMsg()
//            }
//        }
    }
}