package mc.merge.command.argument

import mc.merge.ModCore
import mc.merge.command.ICommandArgument
import mc.merge.command.MatchType
import mc.merge.handler.ChatHandler
import mc.merge.module.Module

class ModuleArgument : ICommandArgument {
    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (ModCore.modules.any { it.name.replace(" ", "").lowercase() == toMatch[0].lowercase() }) {
            MatchType.FULL
        } else if (ModCore.modules.any {
                it.name.replace(" ", "").lowercase().startsWith(toMatch[0].lowercase())
            }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        val modules = ModCore.modules.map { ChatHandler.toCamelCaseName(it.name) }
        if (toMatch.isEmpty()) {
            return modules
        }
        return modules.filter { it.lowercase().startsWith(toMatch[0].lowercase()) }
    }

    fun getModule(toMatch: String): Module? {
        return ModCore.modules.firstOrNull { it.name.replace(" ", "").lowercase() == toMatch.lowercase() }
    }
}