package me.beanbag.nuker.external.rusher

import me.beanbag.nuker.module.Module
import org.rusherhack.client.api.feature.module.ModuleCategory
import org.rusherhack.client.api.feature.module.ToggleableModule
import org.rusherhack.core.setting.NullSetting

class RusherModule(name: String?, description: String?, var module: Module) :
    ToggleableModule(name, description, ModuleCategory.WORLD) {

    init {
        for (settingGroup in module.settingGroups) {
            val rhSettingGroup = NullSetting(settingGroup.name, settingGroup.description)
            settingGroup.settings.forEach{setting ->
                rhSettingGroup.addSubSettings(setting.getRusherSetting())
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