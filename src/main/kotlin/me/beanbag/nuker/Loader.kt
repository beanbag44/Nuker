package me.beanbag.nuker

import me.beanbag.nuker.ModConfigs.LOGGER
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.ModConfigs.meteorIsPresent
import me.beanbag.nuker.ModConfigs.modules
import me.beanbag.nuker.eventsystem.events.PacketEvents
import me.beanbag.nuker.eventsystem.events.RenderEvents
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.modules.nuker.Nuker
import me.beanbag.nuker.utils.FileManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.ActionResult

class Loader : ModInitializer {

    override fun onInitialize() {
        FileManager.loadModuleConfigs()
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        ClientTickEvents.START_CLIENT_TICK.register {
            modules.values.forEach {
                if (it.enabled) {
                    it.onTick()
                }
            }
        }
        RenderEvents.RENDER3D.register {
        }
        PacketEvents.RECEIVE.register { packet ->
            Nuker.onPacketReceive(packet)
            ActionResult.PASS
        }

        LOGGER.info("Initialized $MOD_NAME")
        ChatHandler.setup()
    }
}