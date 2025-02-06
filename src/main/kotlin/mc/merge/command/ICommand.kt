package mc.merge.command


import mc.merge.ModCore.commandPrefix
import net.minecraft.text.Text

interface ICommand {
    val helpText: Text
    val args: List<ICommandArgument>
    fun execute(command: List<String>)

    fun isMatch(command: List<String>): Boolean {
        var commandIndex = 0

        for (arg in args) {
            if (commandIndex >= command.size) {
                return false
            }
            val matchType = arg.getMatch(command.subList(commandIndex, command.size))
            if (matchType != MatchType.FULL) {
                return false
            }
            commandIndex += arg.subArgumentCount
        }
        return commandIndex == command.size
    }

    fun getSuggestions(userInput: String): List<String> {
        val commandParts = userInput.substring(commandPrefix.length).lowercase().split(" ")
        var commandIndex = 0
        var argsIndex = 0

        while (commandIndex < commandParts.size && argsIndex < args.size) {
            val matchType = args[argsIndex].getMatch(commandParts.subList(commandIndex, commandParts.size))
            when (matchType) {
                MatchType.FULL -> {
                    commandIndex += args[argsIndex].subArgumentCount
                    argsIndex ++
                    val endsWithSpace = commandParts.last() == ""
                    if (commandIndex == (commandParts.size - 1) && endsWithSpace && argsIndex < args.size) {
                        return args[argsIndex].getSuggestions(listOf())
                    }
                }
                MatchType.PARTIAL -> {
                    return args[argsIndex].getSuggestions(commandParts.subList(commandIndex, commandParts.size))
                }
                MatchType.NONE -> {
                    return listOf()
                }
            }
        }

        return listOf()
    }
}