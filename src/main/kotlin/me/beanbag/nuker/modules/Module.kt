package me.beanbag.nuker.modules

import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.settings.SettingGroup

abstract class Module(var name: String, var description: String) {
    var settingGroups: MutableList<SettingGroup> = ArrayList()

    var enabled by Setting("Enabled", "Enables the module", true, null) { true }

    protected fun addGroup(setting: SettingGroup): SettingGroup {
        settingGroups.add(setting)
        return setting
    }

    open fun onTick() {
    }
}