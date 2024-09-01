package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.chat.ChatHandler
import me.beanbag.nuker.chat.ChatHandler.sendChatLine
import me.beanbag.nuker.chat.ExecutableClickEvent
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.LiteralArgument
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
        sendChatLine(Text.of("Hovering over modules/settings will show more details.\n"))
        sendChatLine(Text.of("Available commands:\n"))
        for (commandImplementation in ModConfigs.commands) {
            sendChatLine(commandImplementation.helpText)
        }
    }
}