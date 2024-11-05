package me.beanbag.nuker.handlers

import me.beanbag.nuker.eventsystem.events.PacketEvent
import me.beanbag.nuker.eventsystem.events.TickEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket

class InventoryHandler {
    private var ticksTillResume = 0

    fun externalInControl() = ticksTillResume > 0
    init {
        onInGameEvent<TickEvent.Pre> {
            if (ticksTillResume > 0) {
                ticksTillResume--
            }
        }
        onInGameEvent<PacketEvent.Receive.Pre>{ event ->
            val packet = event.packet

            if (packet is UpdateSelectedSlotC2SPacket && BreakingHandler.breakingContexts.none { it?.bestTool == packet.selectedSlot }) {
                ticksTillResume = 24
            } else if (packet is PlayerInteractItemC2SPacket) {
                ticksTillResume = 24
            }
        }
    }
}