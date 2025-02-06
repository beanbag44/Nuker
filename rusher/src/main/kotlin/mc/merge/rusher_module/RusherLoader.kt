package mc.merge.rusher_module

import com.mojang.logging.LogUtils
import mc.merge.ModCore
import mc.merge.ModCore.modId
import mc.merge.ModCore.modName
import mc.merge.event.EventBus
import net.fabricmc.loader.api.FabricLoader
import org.rusherhack.client.api.RusherHackAPI
import org.rusherhack.client.api.plugin.Plugin


@Suppress("unused")
class RusherLoader : Plugin() {
    override fun onLoad() {
        if (!FabricLoader.getInstance().isModLoaded(modId)) { return }

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
        LogUtils.getLogger().info("$modName - Rusher Plugin Loaded")
    }

    override fun onUnload() {
        LogUtils.getLogger().info("$modName Plugin Unloaded!")
    }
}