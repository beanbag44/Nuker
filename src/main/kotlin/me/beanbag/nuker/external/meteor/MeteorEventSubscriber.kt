package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.MeteorRenderEvent
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.render.Render3DEvent
import meteordevelopment.orbit.EventHandler

class MeteorEventSubscriber {
    fun subscribe() {
        MeteorClient.EVENT_BUS.subscribe(this)
    }

    @EventHandler
    fun onRender(event: Render3DEvent) {
        EventBus.post(MeteorRenderEvent(event.renderer))
    }
}