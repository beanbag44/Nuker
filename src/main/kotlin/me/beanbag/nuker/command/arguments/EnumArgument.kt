package me.beanbag.nuker.command.arguments

import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.MatchType

class EnumArgument<T : Enum<T>>(val values: List<T>) : ICommandArgument {

    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (values.any { it.name.lowercase() == toMatch[0].lowercase() }) {
            MatchType.FULL
        } else if (values.any { it.name.lowercase().startsWith(toMatch[0].lowercase()) }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.isEmpty()) {
            return values.map { it.name }
        }
        return values.filter { it.name.lowercase().startsWith(toMatch[0].lowercase()) }.map { it.name }
    }
}