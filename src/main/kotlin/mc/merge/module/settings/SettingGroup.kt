package mc.merge.module.settings

class SettingGroup(var name: String, var description: String, var settings: MutableList<AbstractSetting<*>> = mutableListOf()) {
    fun <T : AbstractSetting<*>> add(setting: T): T {
        settings.add(setting)
        return setting
    }
}