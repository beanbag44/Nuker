package me.beanbag.nuker.command.arguments

import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.MatchType
import me.beanbag.nuker.module.settings.AbstractListSetting
import net.minecraft.block.Blocks

class ModuleSettingListValueArgument : ICommandArgument {
    override val subArgumentCount: Int
        get() = 4

    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }

        val moduleMatch = ModuleArgument().getMatch(toMatch)
        if (moduleMatch == MatchType.NONE || moduleMatch == MatchType.PARTIAL && toMatch.size == 1) {
            return moduleMatch
        }

        val settingMatch = ModuleSettingArgument().getMatch(toMatch)
        if (settingMatch == MatchType.NONE || settingMatch == MatchType.PARTIAL && toMatch.size == 2) {
            return settingMatch
        }

        val listActionMatch = ListActionArgument().getMatch(toMatch.subList(2, toMatch.size))
        if (listActionMatch == MatchType.NONE || listActionMatch == MatchType.PARTIAL && toMatch.size == 3) {
            return listActionMatch
        }

        if (toMatch.size < 4) {
            return MatchType.PARTIAL
        }
        try{ ListAction.valueOf(toMatch[2].lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) } catch (e: IllegalArgumentException) { null }?: return MatchType.PARTIAL
        val module = ModuleArgument().getModule(toMatch[0])?: return MatchType.PARTIAL
        val setting = ModuleSettingArgument().getSetting(module, toMatch[1])?: return MatchType.PARTIAL
        if (setting !is AbstractListSetting<*>) {
            return MatchType.NONE
        }
        val value = setting.valueFromString(toMatch[3])
        if (value != null && value != listOf(Blocks.AIR)) {
            return MatchType.FULL
        }
        if (setting.possibleValues()?.any { it.lowercase().startsWith(toMatch[3].lowercase()) } == true) {
            return MatchType.PARTIAL
        }
        return MatchType.NONE
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.size < 2) {
            return ModuleArgument().getSuggestions(toMatch)
        }
        val module = ModuleArgument().getModule(toMatch[0]) ?: return listOf()

        if (toMatch.size == 2) {
            return ModuleSettingArgument().getSuggestions(toMatch)
        }
        val setting = ModuleSettingArgument().getSetting(module, toMatch[1]) ?: return listOf()

        if (setting !is AbstractListSetting<*>) {
            return listOf()
        }
        if (toMatch.size == 3 && ModuleSettingArgument().getMatch(toMatch) == MatchType.FULL) {
            return ListActionArgument().getSuggestions(toMatch.subList(2, toMatch.size))
        }


        val allPossibleValues = setting.possibleValues()
        return allPossibleValues?.filter { it.lowercase().startsWith(toMatch[3].lowercase()) } ?: listOf()
    }
}