package mc.merge.command.argument

import mc.merge.command.ICommandArgument
import mc.merge.command.MatchType

class LiteralArgument(private val argument:String) : ICommandArgument {
    override fun getMatch(toMatch: List<String>): MatchType {
        if (toMatch.isEmpty()) {
            return MatchType.NONE
        }
        return if (toMatch[0] == argument) {
            MatchType.FULL
        } else if (argument.startsWith(toMatch[0])) {
            MatchType.PARTIAL
        } else {
            MatchType.NONE
        }
    }

    override fun getSuggestions(toMatch: List<String>): List<String> {
        if (toMatch.isEmpty() || argument.startsWith(toMatch[0])) {
            return listOf(argument)
        }
        return listOf()
    }
}