package me.beanbag.nuker.external.rusher

import com.mojang.logging.LogUtils
import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.eventsystem.EventBus
import org.rusherhack.client.api.RusherHackAPI
import org.rusherhack.client.api.plugin.Plugin

@Suppress("unused")
class RusherLoader : Plugin() {
    override fun onLoad() {
        for (module in ModConfigs.modules) {
            LogUtils.getLogger().info("Loading module: ${module.name}")

            val rusherModule = RusherModule(module.name.replace(" ", ""), module.description, module)
            RusherHackAPI.getModuleManager().registerFeature(rusherModule)

            if (!module.enabled) {
                EventBus.unsubscribe(module)
            }
        }
        RusherHackAPI.getModuleManager().registerFeature(ListenerModule())
        RusherHackAPI.getCommandManager().registerFeature(RusherCommands())

        ModConfigs.rusherIsPresent = true
        LogUtils.getLogger().info("$MOD_NAME Plugin Loaded")
    }

    override fun onUnload() {
        LogUtils.getLogger().info("$MOD_NAME Plugin Unloaded!")
    }
}