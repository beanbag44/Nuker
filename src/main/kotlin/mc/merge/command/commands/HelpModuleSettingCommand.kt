package mc.merge.command.commands

import mc.merge.ModCore.commandPrefix
import mc.merge.command.ICommand
import mc.merge.command.ICommandArgument
import mc.merge.command.argument.LiteralArgument
import mc.merge.command.argument.ModuleArgument
import mc.merge.command.argument.ModuleSettingArgument
import mc.merge.handler.ChatHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModuleSettingCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${commandPrefix}help [module] [setting]")
            .append(Text.literal(" - Lists details about the setting").styled {
                it.withColor(Formatting.GRAY)
            })

    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("help"), ModuleSettingArgument())

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        val module = ModuleArgument().getModule(command[1])?: return
        val setting = ModuleSettingArgument().getSetting(module, command[2])?: return
        ChatHandler.sendChatLine(setting.helpText())
    }
}
