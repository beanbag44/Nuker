package me.beanbag.nuker.external.meteor

import com.mojang.logging.LogUtils
import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.MOD_NAME
import meteordevelopment.meteorclient.addons.MeteorAddon
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.systems.modules.Modules

class MeteorLoader : MeteorAddon() {

    companion object{
        val CATEGORY: Category = Category(MOD_NAME)
    }

    override fun onInitialize() {
        LogUtils.getLogger().info("Initializing CanalTools Addon")

        for (module in ModConfigs.modules.values) {
            val meteorModule: Module = MeteorModule(module)
            Modules.get().add(meteorModule)
        }
    }

    override fun onRegisterCategories() {
        Modules.registerCategory(CATEGORY)
    }

    override fun getPackage(): String {
        return "me.beanbag.nuker.external.meteor"
    }
}