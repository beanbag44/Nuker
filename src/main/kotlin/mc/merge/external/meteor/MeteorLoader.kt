package mc.merge.external.meteor

import com.mojang.logging.LogUtils
import mc.merge.ModCore.modName
import meteordevelopment.meteorclient.addons.MeteorAddon
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Modules

class MeteorLoader : MeteorAddon() {

    companion object{
        val CATEGORY: Category = Category(modName)
    }

    override fun onInitialize() {
        LogUtils.getLogger().info("Initializing $modName Addon")

        for (module in MeteorModule.modules) {
            Modules.get().add(module)
        }
        MeteorEventSubscriber().subscribe()
    }

    override fun onRegisterCategories() {
        Modules.registerCategory(CATEGORY)
    }

    override fun getPackage(): String {
        return "mc.merge"
    }
}
