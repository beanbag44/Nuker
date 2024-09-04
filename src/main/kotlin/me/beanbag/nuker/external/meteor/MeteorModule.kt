package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.module.Module
import meteordevelopment.meteorclient.systems.modules.Module as MeteorModule

class MeteorModule(var module: Module) : MeteorModule(MeteorLoader.CATEGORY, module.name, module.description) {

    init {
        for (settingGroup in module.settingGroups) {
            val group = settings.createGroup(settingGroup.name)
            for (setting in settingGroup.settings) {
                group.add(setting.toMeteorSetting())
            }
        }
    }

    override fun onActivate() {
        module.enabled = true
    }

    override fun onDeactivate() {
        module.enabled = false
    }
}