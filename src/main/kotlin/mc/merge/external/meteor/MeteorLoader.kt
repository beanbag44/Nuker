package mc.merge.external.meteor

import com.mojang.logging.LogUtils
import mc.merge.ModCore.modName
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.addons.MeteorAddon
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory
import meteordevelopment.meteorclient.gui.widgets.containers.WTable
import meteordevelopment.meteorclient.settings.Setting
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Modules

class MeteorLoader : MeteorAddon() {

    companion object{
        val CATEGORY: Category = Category(modName)
    }

    override fun onInitialize() {
        LogUtils.getLogger().info("Initializing $modName Addon")

        for (module in MeteorModule.meteorModules) {
            Modules.get().add(module)
        }

        SettingsWidgetFactory.registerCustomFactory(MeteorBlockPresetSetting::class.java) { theme: GuiTheme? ->
            SettingsWidgetFactory.Factory { table: WTable?, setting: Setting<*>? ->
                val button = table!!.add(theme!!.button("Select")).expandCellX().widget()
                if (button != null) {
                    button.action = Runnable {
                        MeteorClient.mc.setScreen(
                            MeteorBlockPresetSettingScreen(
                                theme,
                                setting as MeteorBlockPresetSetting?
                            )
                        )
                    }
                }
            }
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
