package me.beanbag.nuker

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import me.beanbag.nuker.events.PacketEvents
import me.beanbag.nuker.events.RenderEvents
import me.beanbag.nuker.chat.ChatHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import me.beanbag.nuker.modules.Module
import me.beanbag.nuker.modules.nuker.Nuker
import net.minecraft.util.ActionResult

class Loader : ModInitializer {

    companion object {
        val mc: MinecraftClient = MinecraftClient.getInstance()
        var meteorIsPresent = false
        var rusherIsPresent = false
        val LOGGER: Logger = LoggerFactory.getLogger("Nuker")

        var modules: MutableMap<Class<out Module>, Module> =
            listOf(Nuker).associateByTo(Reference2ReferenceOpenHashMap()) { it.javaClass }
    }

    override fun onInitialize() {
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

        LOGGER.info("Initialized Nuker!")
        ChatHandler.setup()
    }
}