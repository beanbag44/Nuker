package me.beanbag.nuker.events

import me.beanbag.nuker.events.PacketEvents.PacketReceive
import me.beanbag.nuker.events.PacketEvents.PacketSend
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.packet.Packet
import net.minecraft.util.ActionResult

object PacketEvents {
    @JvmField
    var SEND: Event<PacketSend> = EventFactory.createArrayBacked(
        PacketSend::class.java
    ) { listeners: Array<PacketSend> ->
        PacketSend { packet: Packet<*> ->
            listeners.forEach { listener ->
                val result = listener.invoke(packet)
                if (result != ActionResult.PASS) {
                    return@PacketSend result
                }
            }
            return@PacketSend ActionResult.PASS
        }
    }

    @JvmField
    var RECEIVE: Event<PacketReceive> = EventFactory.createArrayBacked(
        PacketReceive ::class.java)
    { listeners: Array<PacketReceive> ->
        PacketReceive { packet: Packet<*> ->
            listeners.forEach { listener ->
                val result = listener.invoke(packet)
                if (result != ActionResult.PASS) {
                    return@PacketReceive result
                }
            }
            return@PacketReceive ActionResult.PASS
        }
    }


    // make it functional interface (SAM: Simple Abstract Method interface)
    fun interface PacketSend {
        operator fun invoke(packet: Packet<*>): ActionResult
    }

    fun interface PacketReceive {
        operator fun invoke(packet: Packet<*>): ActionResult
    }
}