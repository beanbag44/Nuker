package me.beanbag.nuker.command.arguments

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.MatchType
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.Module

class ModuleArgument : ICommandArgument {
    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (ModConfigs.modules.values.any { it.name.replace(" ", "").lowercase() == toMatch[0].lowercase() }) {
            MatchType.FULL
        } else if (ModConfigs.modules.values.any {
                it.name.replace(" ", "").lowercase().startsWith(toMatch[0].lowercase())
            }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        val modules = ModConfigs.modules.values.map { ChatHandler.toCamelCaseName(it.name) }
        if (toMatch.isEmpty()) {
            return modules
        }
        return modules.filter { it.lowercase().startsWith(toMatch[0].lowercase()) }
    }

    fun getModule(toMatch: String): Module? {
        return ModConfigs.modules.values.firstOrNull { it.name.replace(" ", "").lowercase() == toMatch.lowercase() }
    }
}