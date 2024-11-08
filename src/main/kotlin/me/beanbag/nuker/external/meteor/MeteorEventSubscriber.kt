package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.RenderEvent
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.render.Render3DEvent
import meteordevelopment.orbit.EventHandler

class MeteorEventSubscriber {
    fun subscribe() {
        MeteorClient.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun onRender(event: Render3DEvent) {
//        if (!ModConfigs.rusherIsPresent) { //TODO add this back in when we can hook into rusher's render event
            EventBus.post(RenderEvent.Render3DEvent(MeteorRenderer3D(event.renderer)))
//        }
    }
}