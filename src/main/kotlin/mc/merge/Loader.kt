package mc.merge

import mc.merge.ModCore.LOGGER
import mc.merge.ModCore.MOD_NAME
import mc.merge.ModCore.meteorIsLoaded
import mc.merge.ModCore.meteorIsPresent
import mc.merge.event.EventBus
import mc.merge.util.FileManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

class Loader: ModInitializer {
    override fun onInitialize() {
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        LOGGER.info("Initialized $MOD_NAME")
        tryInitialize()
        println("Trying to initialize from ModInitializer")
    }

    //Do not use. Use CommonLoader instead.
    companion object {

        fun tryInitialize() {
            if (meteorIsPresent && !meteorIsLoaded) {
                return
            }
//            GUI.initGUI()
            FileManager.loadModuleConfigs()
            for (module in ModCore.modules) {
                if (!module.enabled) {
                    EventBus.unsubscribe(module)
                }
            }
        }
    }
}