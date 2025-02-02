package mc.merge.command.commands

import mc.merge.ModCore
import mc.merge.ModCore.COMMAND_PREFIX
import mc.merge.command.ExecutableClickEvent
import mc.merge.command.ICommand
import mc.merge.command.ICommandArgument
import mc.merge.command.argument.LiteralArgument
import mc.merge.handler.ChatHandler
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpCommand : ICommand {
    override val helpText: Text
        get() = Text.empty().append(Text.literal("${COMMAND_PREFIX}help").styled {
            it.withClickEvent(ExecutableClickEvent { execute(emptyList()) })
                .withUnderline(true)
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to run this command")))
        }).append(Text.literal(" - Shows this list").styled { it.withColor(Formatting.GRAY) })


    override val args: List<ICommandArgument> = listOf(LiteralArgument("help"))

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        ChatHandler.sendChatLine("Hovering over modules/settings will show more details.\n")
        ChatHandler.sendChatLine("Available commands:\n")
        for (commandImplementation in ModCore.commands) {
            ChatHandler.sendChatLine(commandImplementation.helpText)
        }
    }
}