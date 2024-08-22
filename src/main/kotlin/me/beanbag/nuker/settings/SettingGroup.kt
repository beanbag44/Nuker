package me.beanbag.nuker.settings

class SettingGroup(var name: String, var description: String, var settings: ArrayList<Setting<*>> = ArrayList()) {
    fun <T : Setting<*>> add(setting: T): T {
        settings.add(setting)
        return setting
    }
}