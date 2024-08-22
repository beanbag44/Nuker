package me.beanbag.nuker.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

object RenderEvents {

    @JvmField
    var RENDER3D: Event<Render3D> = EventFactory.createArrayBacked(
        Render3D ::class.java)
    { listeners: Array<Render3D> ->
        Render3D { listeners.forEach { it.invoke()} }
    }

    fun interface Render3D {
        operator fun invoke()
    }
}