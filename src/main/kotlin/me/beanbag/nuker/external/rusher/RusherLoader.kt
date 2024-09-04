package me.beanbag.nuker.external.rusher

import com.mojang.logging.LogUtils
import me.beanbag.nuker.ModConfigs
import org.rusherhack.client.api.RusherHackAPI
import org.rusherhack.client.api.plugin.Plugin

class RusherLoader : Plugin() {
    override fun onLoad() {
        for (module in ModConfigs.modules.values) {
            LogUtils.getLogger().info("Loading module: ${module.name}")

            val rusherModule = RusherModule(module.name, module.description, module)
            RusherHackAPI.getModuleManager().registerFeature(rusherModule)
        }
        ModConfigs.rusherIsPresent = true
        LogUtils.getLogger().info("CanalTools Plugin Loaded")
    }

    override fun onUnload() {
        LogUtils.getLogger().info("CanalTools Plugin Unloaded!")
    }
}