package me.beanbag.nuker.external.rusher

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.PlayerMoveEvent
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.network.packet.Packet
import org.rusherhack.client.api.events.client.EventQuit
import org.rusherhack.client.api.events.client.EventUpdate
import org.rusherhack.client.api.events.network.EventPacket
import org.rusherhack.client.api.events.player.EventMove
import org.rusherhack.client.api.events.render.EventRender3D
import org.rusherhack.client.api.feature.module.Module
import org.rusherhack.client.api.feature.module.ModuleCategory
import org.rusherhack.core.event.stage.Stage
import org.rusherhack.core.event.subscribe.Subscribe

@Suppress("unused", "UNUSED_PARAMETER")
class ListenerModule : Module("Listener Module", ModuleCategory.CLIENT) {
    init {
        this.isHidden = true
    }

    @Subscribe(stage = Stage.PRE)
    fun onTickPre(event: EventUpdate) {
        EventBus.post(TickEvent.Pre())
    }

    @Subscribe(stage = Stage.POST)
    fun onTickPost(event: EventUpdate) {
        EventBus.post(TickEvent.Post())
    }

    @Subscribe
    fun onRender(event: EventRender3D) {
        EventBus.post(
            RenderEvent.Render3DEvent(
                RusherRenderer3D(
                    event.renderer,
                    event.javaClass.superclass.getMethod("getMatrixStack").invoke(event) as MatrixStack
                )
            )
        )
    }

    @Subscribe
    fun onPacketSend(event: EventPacket.Send) {
        val nukerEvent = PacketEvent.Send.Pre(event.javaClass.getMethod("getPacket").invoke(event) as Packet<*>)
        EventBus.post(nukerEvent)
        if (nukerEvent.isCanceled()) event.isCancelled = true
    }

    @Subscribe
    fun onPacketReceive(event: EventPacket.Receive) {
        val nukerEvent = PacketEvent.Receive.Pre(event.javaClass.getMethod("getPacket").invoke(event) as Packet<*>)
        EventBus.post(nukerEvent)
        if (nukerEvent.isCanceled()) event.isCancelled = true
    }

    @Subscribe
    fun onGameLeft(event: EventQuit) {
        EventBus.post(me.beanbag.nuker.eventsystem.events.GameQuitEvent())
    }

    @Subscribe
    fun onPlayerMove(event: EventMove) {
        EventBus.post(PlayerMoveEvent())
    }
}