package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ChatHandler
import me.beanbag.nuker.command.ChatHandler.sendChatLine
import me.beanbag.nuker.command.ChatHandler.toCamelCaseName
import me.beanbag.nuker.command.ExecutableClickEvent
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModulesCommand: ICommand {
    override val helpText: Text
        get() = Text.empty().append(Text.literal("${COMMAND_PREFIX}help modules").styled {
            it.withClickEvent(ExecutableClickEvent {
                execute(emptyList())
            })
                .withUnderline(true)
                .withHoverEvent(
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to run this command"))
                )
        }).append(Text.literal(" - Lists modules and their descriptions").styled { it.withColor(Formatting.GRAY) })

    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("help"), LiteralArgument("modules"))

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        sendChatLine(Text.of("Click on a module or type \"${COMMAND_PREFIX}help [module]\" for more details"))
        for (loaderModule in ModConfigs.modules.values) {
            sendChatLine(
                Text.empty().append(Text.literal(toCamelCaseName(loaderModule.name)).styled {
                    it.withClickEvent(ExecutableClickEvent {
                        sendChatLine(loaderModule.helpText())
                    })
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, loaderModule.helpText()))
                        .withUnderline(true)
                }).append(Text.literal(" - ${loaderModule.description}").styled { it.withColor(Formatting.GRAY) })
            )
        }
    }
}
