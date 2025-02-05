package mc.merge.rusher_module

import com.mojang.logging.LogUtils
import mc.merge.ModCore
import mc.merge.ModCore.MOD_ID
import mc.merge.ModCore.MOD_NAME
import mc.merge.event.EventBus
import net.fabricmc.loader.api.FabricLoader
import org.rusherhack.client.api.RusherHackAPI
import org.rusherhack.client.api.plugin.Plugin


@Suppress("unused")
class RusherLoader : Plugin() {
    override fun onLoad() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) { return }

        for (module in ModCore.modules) {
            LogUtils.getLogger().info("Loading module: ${module.name}")

            val rusherModule = RusherModule(module.name.replace(" ", ""), module.description, module)
            RusherHackAPI.getModuleManager().registerFeature(rusherModule)

            if (!module.enabled) {
                EventBus.unsubscribe(module)
            }
        }
        RusherHackAPI.getModuleManager().registerFeature(ListenerModule())

        ModCore.rusherIsPresent = true
        LogUtils.getLogger().info("$MOD_NAME - Rusher Plugin Loaded")
    }

    override fun onUnload() {
        LogUtils.getLogger().info("$MOD_NAME Plugin Unloaded!")
    }
}