package me.beanbag.nuker.external.rusher

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import net.minecraft.network.packet.Packet
import org.rusherhack.client.api.events.client.EventUpdate
import org.rusherhack.client.api.events.network.EventPacket
import org.rusherhack.client.api.events.render.EventRender3D
import org.rusherhack.client.api.feature.module.Module
import org.rusherhack.client.api.feature.module.ModuleCategory
import org.rusherhack.core.event.stage.Stage
import org.rusherhack.core.event.subscribe.Subscribe

class ListenerModule : Module("Listener Module", ModuleCategory.CLIENT) {
    init {
        this.isHidden = true
    }

    @Subscribe
    fun onTick(event: EventUpdate) {
        when (event.stage) {
            Stage.ON -> EventBus.post(TickEvent.Pre())
            else -> {}
        }
    }

    @Subscribe
    fun onRender(event: EventRender3D) {
        EventBus.post(RenderEvent(RusherRenderer()))
    }

    @Subscribe
    fun onPacketSend(event: EventPacket.Send) {
        EventBus.post(PacketEvent.Send.Pre(event.javaClass.getMethod("getPacket").invoke(event) as Packet<*>))
    }

    @Subscribe
    fun onPacketReceive(event: EventPacket.Receive) {
        EventBus.post(PacketEvent.Receive.Pre(event.javaClass.getMethod("getPacket").invoke(event) as Packet<*>))
    }
}