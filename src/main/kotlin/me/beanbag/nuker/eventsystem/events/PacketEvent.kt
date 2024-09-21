package me.beanbag.nuker.eventsystem.events

import net.minecraft.network.packet.Packet

abstract class PacketEvent(
    val packet: Packet<*>
) : Event {
    sealed class Send(packet: Packet<*>) : PacketEvent(packet) {
        class Pre(packet: Packet<*>) : Send(packet), ICancellable by Cancellable()
        class Post(packet: Packet<*>) : Send(packet)
    }

    sealed class Receive(packet: Packet<*>) : PacketEvent(packet) {
        class Pre(packet: Packet<*>) : Receive(packet), ICancellable by Cancellable()
        class Post(packet: Packet<*>) : Receive(packet)
    }
}