package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.Loader
import me.beanbag.nuker.chat.ChatHandler.COMMAND_PREFIX
import me.beanbag.nuker.chat.ChatHandler.printHeader
import me.beanbag.nuker.chat.ChatHandler.sendChatLine
import me.beanbag.nuker.chat.ExecutableClickEvent
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.LiteralArgument
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ListCommand : ICommand {
    override val helpText: Text
        get() = Text.empty().append(Text.literal("${COMMAND_PREFIX}list").styled {
            it.withClickEvent(ExecutableClickEvent {
                execute(emptyList())
            })
                .withUnderline(true)
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to run this command")))
        }).append(Text.literal(" - Lists modules and if they are on or off").styled { it.withColor(Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(LiteralArgument("list"))

    override fun execute(command: List<String>) {
        printHeader()
        for (loaderModule in Loader.modules.values) {
            sendChatLine(Text.empty().append(Text.literal(loaderModule.name).styled {
                it.withClickEvent(ExecutableClickEvent {
                    ListModuleCommand().execute(listOf(loaderModule.name))
                })
                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, loaderModule.helpText()))
                    .withUnderline(true)
            }).append(Text.literal(" - ").styled { it.withColor(Formatting.GRAY) }).append(loaderModule.enabledText()))
        }
    }
}