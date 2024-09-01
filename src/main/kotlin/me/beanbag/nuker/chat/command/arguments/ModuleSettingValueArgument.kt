package me.beanbag.nuker.chat.command.arguments

import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.MatchType

class ModuleSettingValueArgument : ICommandArgument {
    override val placeholder: String
        get() = "[module] [setting]"
    override val subArgumentCount: Int
        get() = 2

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
        //TODO: check if value can be parsed to setting type
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

        //TODO: Implement getting setting values
        return listOf("TODO")
    }
}
