package me.beanbag.nuker.chat.command.arguments

import me.beanbag.nuker.Loader
import me.beanbag.nuker.chat.ChatHandler
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.MatchType
import me.beanbag.nuker.modules.Module

class ModuleArgument : ICommandArgument {
    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (Loader.modules.values.any { it.name.replace(" ", "").lowercase() == toMatch[0].lowercase() }) {
            MatchType.FULL
        } else if (Loader.modules.values.any {
                it.name.replace(" ", "").lowercase().startsWith(toMatch[0].lowercase())
            }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        val modules = Loader.modules.values.map { ChatHandler.toCamelCaseName(it.name) }
        if (toMatch.isEmpty()) {
            return modules
        }
        return modules.filter { it.lowercase().startsWith(toMatch[0].lowercase()) }
    }

    fun getModule(toMatch: String): Module? {
        return Loader.modules.values.firstOrNull { it.name.replace(" ", "").lowercase() == toMatch.lowercase() }
    }
}