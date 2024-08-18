package me.beanbag.nuker

import me.beanbag.nuker.Nuker.LOGGER
import me.beanbag.nuker.Nuker.meteorIsPresent
import me.beanbag.nuker.Nuker.onPacketReceive
import me.beanbag.nuker.Nuker.onTick
import me.beanbag.nuker.events.PacketReceiveCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.ActionResult

class Loader : ModInitializer {
    override fun onInitialize() {
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        ClientTickEvents.START_CLIENT_TICK.register { onTick() }
        PacketReceiveCallback.EVENT.register { packet ->
            onPacketReceive(packet)
            return@register ActionResult.PASS
        }

        LOGGER.info("Initialized Nuker!")
    }
}