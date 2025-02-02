package mc.merge.rusher_module

import mc.merge.module.Module
import org.rusherhack.client.api.feature.module.ModuleCategory
import org.rusherhack.client.api.feature.module.ToggleableModule
import org.rusherhack.core.setting.NullSetting

class RusherModule(name: String?, description: String?, var module: Module) :
    ToggleableModule(name, description, ModuleCategory.WORLD) {

    init {
        val settingBuilder = RusherSettingBuilder()
        for (settingGroup in module.settingGroups) {
            val rhSettingGroup = NullSetting(settingGroup.name, settingGroup.description)
            settingGroup.settings.forEach { setting ->
                rhSettingGroup.addSubSettings(settingBuilder.toRusherSetting(setting))
            }
            registerSettings(rhSettingGroup)
        }

        module.enabledSetting.getOnChange().add { value -> if (this.isToggled != value) this.toggle() }
    }

    override fun onEnable() {
        module.enabled = true
    }

    override fun onDisable() {
        module.enabled = false
    }
}