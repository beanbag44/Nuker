package me.beanbag.nuker.external.rusher

import com.mojang.logging.LogUtils
import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.MOD_NAME
import org.rusherhack.client.api.RusherHackAPI
import org.rusherhack.client.api.plugin.Plugin

@Suppress("unused")
class RusherLoader : Plugin() {
    override fun onLoad() {
        for (module in ModConfigs.modules.values) {
            LogUtils.getLogger().info("Loading module: ${module.name}")

            val rusherModule = RusherModule(module.name, module.description, module)
            RusherHackAPI.getModuleManager().registerFeature(rusherModule)
        }
        RusherHackAPI.getCommandManager().registerFeature(RusherCommands())

        ModConfigs.rusherIsPresent = true
        LogUtils.getLogger().info("$MOD_NAME Plugin Loaded")
    }

    override fun onUnload() {
        LogUtils.getLogger().info("$MOD_NAME Plugin Unloaded!")
    }
}