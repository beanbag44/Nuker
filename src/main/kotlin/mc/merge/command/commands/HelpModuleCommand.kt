package mc.merge.command.commands

import mc.merge.ModCore.COMMAND_PREFIX
import mc.merge.command.ICommand
import mc.merge.command.ICommandArgument
import mc.merge.command.argument.LiteralArgument
import mc.merge.command.argument.ModuleArgument
import mc.merge.handler.ChatHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModuleCommand : ICommand {
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
        val module = ModuleArgument().getModule(command[1])
        module?.let { ChatHandler.sendChatLine(it.helpText()) }
    }
}
