package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.chat.ChatHandler
import me.beanbag.nuker.chat.ChatHandler.COMMAND_PREFIX
import me.beanbag.nuker.chat.ChatHandler.sendChatLine
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.LiteralArgument
import me.beanbag.nuker.chat.command.arguments.ModuleArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModuleCommand : ICommand{
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}help [module]")
            .append(Text.literal(" - Lists module settings and their descriptions").styled {
                it.withColor(
                    Formatting.GRAY
                )
            })

    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("help"), ModuleArgument())

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        val module = ModuleArgument().getModule(command[1])!!
        sendChatLine(module.helpText())
    }
}
