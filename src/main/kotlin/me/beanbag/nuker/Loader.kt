package me.beanbag.nuker

import me.beanbag.nuker.ModConfigs.LOGGER
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.ModConfigs.meteorIsLoaded
import me.beanbag.nuker.ModConfigs.meteorIsPresent
import me.beanbag.nuker.eventsystem.EventBus
//import me.beanbag.nuker.render.gui.GUI
import me.beanbag.nuker.utils.FileManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

class Loader : ModInitializer {

    override fun onInitialize() {
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        LOGGER.info("Initialized $MOD_NAME")
        tryInitialize()
        println("Trying to initialize from ModInitializer")
    }

    companion object {
        fun tryInitialize() {
            if (meteorIsPresent && !meteorIsLoaded) {
                return
            }
//            GUI.initGUI()
            FileManager.loadModuleConfigs()
            for (module in ModConfigs.modules.values) {
                if (!module.enabled) {
                    EventBus.unsubscribe(module)
                }
            }
        }
    }
}