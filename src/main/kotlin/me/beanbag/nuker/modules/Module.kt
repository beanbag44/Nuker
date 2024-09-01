package me.beanbag.nuker.modules

import me.beanbag.nuker.chat.ChatHandler.modColor
import me.beanbag.nuker.chat.ChatHandler.toCamelCaseName
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.settings.SettingGroup
import net.minecraft.text.Text
import net.minecraft.util.Formatting

abstract class Module(var name: String, var description: String) {
    var settingGroups: MutableList<SettingGroup> = ArrayList()

    var enabled by Setting("Enabled", "Enables the module", true, null) { true }

    protected fun addGroup(setting: SettingGroup): SettingGroup {
        settingGroups.add(setting)
        return setting
    }

    open fun onTick() {
    }

    fun helpText(): Text {
        val text = Text.literal("${toCamelCaseName(name)} - ${description}\n")

        for(settingGroup in settingGroups) {
            text.append(Text.literal(settingGroup.name).styled { it.withColor(modColor) }).append(Text.literal(" - ${settingGroup.description}\n").styled { it.withColor(
                Formatting.GRAY) })
            for(setting in settingGroup.settings) {
                text.append(Text.literal(" " + toCamelCaseName(setting.getName()))).append(Text.literal(" - ${setting.getDescription()}\n").styled { it.withColor(
                    Formatting.GRAY) })
            }
        }
        return text
    }

    fun enabledText() : Text {
        return Text.literal(if (enabled) "Enabled" else "Disabled").styled { it.withColor(if (enabled) Formatting.GREEN else Formatting.RED) }
    }
}