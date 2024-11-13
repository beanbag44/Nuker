package me.beanbag.nuker.command.arguments

import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.MatchType

class ListActionArgument  : ICommandArgument {

    private val listActions = ListAction.entries
    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (listActions.any { it.name.lowercase() == toMatch[0].lowercase() }) {
            MatchType.FULL
        } else if (listActions.any { it.name.lowercase().startsWith(toMatch[0].lowercase()) }) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.isEmpty()) {
            return listActions.map { it.name }
        }
        return listActions.filter { it.name.lowercase().startsWith(toMatch[0].lowercase()) }.map { it.name }
    }


}

enum class ListAction {
    Add,
    Remove,
}