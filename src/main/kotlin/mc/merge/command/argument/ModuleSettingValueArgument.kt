package mc.merge.command.argument

import mc.merge.command.ICommandArgument
import mc.merge.command.MatchType
import mc.merge.module.settings.AbstractListSetting

class ModuleSettingValueArgument : ICommandArgument {
    override val subArgumentCount: Int
        get() = 3

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

        if (toMatch.size < 3) {
            return MatchType.PARTIAL
        }
        val module = ModuleArgument().getModule(toMatch[0])?: return MatchType.PARTIAL
        val setting = ModuleSettingArgument().getSetting(module, toMatch[1])?: return MatchType.PARTIAL
        if (setting is AbstractListSetting<*>) return MatchType.NONE
        val value = setting.valueFromString(toMatch[2])
        if (value != null) {
            return MatchType.FULL
        }
        if (setting.possibleValues()?.any { it.lowercase().startsWith(toMatch[2].lowercase()) } == true) {
            return MatchType.PARTIAL
        }
        return MatchType.NONE
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.size < 2) {
            return ModuleArgument().getSuggestions(toMatch)
        } else if (toMatch.size == 2) {
            return ModuleSettingArgument().getSuggestions(toMatch)
        }

        val module = ModuleArgument().getModule(toMatch[0]) ?: return listOf()
        val setting = ModuleSettingArgument().getSetting(module, toMatch[1]) ?: return listOf()

        val allPossibleValues = setting.possibleValues()
        return allPossibleValues?.filter { it.lowercase().startsWith(toMatch[2].lowercase()) } ?: listOf()
    }
}
