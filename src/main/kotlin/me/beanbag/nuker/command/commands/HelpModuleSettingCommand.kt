package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import me.beanbag.nuker.command.arguments.ModuleSettingArgument
import me.beanbag.nuker.handlers.ChatHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModuleSettingCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}help [module] [setting]")
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
