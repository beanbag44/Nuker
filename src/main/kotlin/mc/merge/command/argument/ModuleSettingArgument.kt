package mc.merge.command.argument

import mc.merge.command.ICommandArgument
import mc.merge.command.MatchType
import mc.merge.handler.ChatHandler
import mc.merge.module.Module
import mc.merge.module.settings.AbstractSetting

class ModuleSettingArgument :
    ICommandArgument {
    override val subArgumentCount: Int
        get() = 2

    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.size < 2) {
            return MatchType.NONE
        }
        val module = ModuleArgument().getModule(toMatch[0]) ?: return MatchType.NONE

        return if (module.settingGroups.flatMap { it.settings }
                .any { it.getName().replace(" ", "").equals(toMatch[1], ignoreCase = true) }) {
            MatchType.FULL
        } else if (module.settingGroups.flatMap { it.settings }
                .any { it.getName().replace(" ", "").startsWith(toMatch[1], ignoreCase = true) }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.size < 2) {
            return ModuleArgument().getSuggestions(toMatch)
        }
        val module = ModuleArgument().getModule(toMatch[0]) ?: return listOf()
        val allSettings = module.settingGroups.flatMap { it.settings }.map { ChatHandler.toCamelCaseName(it.getName()) }
        return allSettings.filter { it.lowercase().startsWith(toMatch[1].lowercase()) }
    }

    fun getSetting(module: Module, toMatch: String): AbstractSetting<*>? {
        return module.settingGroups.flatMap { it.settings }
            .firstOrNull { it.getName().replace(" ", "").lowercase() == toMatch.lowercase() }
    }
}
