package me.beanbag.nuker.module.modules

import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.Module
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket

object BreakPacketLogger: Module(
    "Break Packet Logger",
    "Logs break packets"
) {
    init {
        EventBus.subscribe<PacketEvent.Send.Pre>(this) { event ->
            if (!enabled) return@subscribe
            if (event.packet !is PlayerActionC2SPacket) return@subscribe
            val packet = event.packet
            val action = packet.action
            if (action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                && action != PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && action != PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK
                ) {
                return@subscribe
            }

            if (packet.sequence != 0) {
                event.cancel()
                mc.networkHandler?.sendPacket(PlayerActionC2SPacket(action, packet.pos, packet.direction, 0))
                return@subscribe
            }

            ChatHandler.sendChatLine(packet.action.toString())
            ChatHandler.sendChatLine("   " + packet.pos.toString())
//            ChatHandler.sendChatLine("   " + packet.direction.toString())
//            ChatHandler.sendChatLine("   " + packet.sequence.toString())
        }
    }
}