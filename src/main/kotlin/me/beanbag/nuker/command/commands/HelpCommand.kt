package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ExecutableClickEvent
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import me.beanbag.nuker.handlers.ChatHandler
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
        ChatHandler.sendChatLine(Text.of("Hovering over modules/settings will show more details.\n"))
        ChatHandler.sendChatLine(Text.of("Available commands:\n"))
        for (commandImplementation in ModConfigs.commands) {
            ChatHandler.sendChatLine(commandImplementation.helpText)
        }
    }
}