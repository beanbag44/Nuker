package me.beanbag.nuker

import me.beanbag.nuker.ModConfigs.LOGGER
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.ModConfigs.meteorIsPresent
import me.beanbag.nuker.utils.FileManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

class Loader : ModInitializer {

    override fun onInitialize() {
        FileManager.loadModuleConfigs()
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        LOGGER.info("Initialized $MOD_NAME")
    }
}