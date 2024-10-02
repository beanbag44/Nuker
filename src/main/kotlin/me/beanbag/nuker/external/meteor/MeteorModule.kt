package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.settings.BoolSetting
import java.util.function.Consumer
import meteordevelopment.meteorclient.systems.modules.Module as MeteorModule

class MeteorModule(var module: Module) : MeteorModule(MeteorLoader.CATEGORY, module.name, module.description) {

    init {
        for (settingGroup in module.settingGroups) {
            val group = settings.createGroup(settingGroup.name)
            for (setting in settingGroup.settings) {
                group.add(setting.getMeteorSetting())
            }
        }
        module.enabledSetting.getOnChange().add(Consumer{ value -> if(this.isActive != value) this.toggle()})
    }

    override fun onActivate() {
        module.enabled = true
    }

    override fun onDeactivate() {
        module.enabled = false
    }
}