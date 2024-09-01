package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.LiteralArgument
import me.beanbag.nuker.chat.command.arguments.ModuleSettingArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class HelpModuleSettingCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}help [module] [setting]").append(Text.literal(" - Lists details about the setting").styled { it.withColor(
            Formatting.GRAY) })

    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("help"), ModuleSettingArgument())

    override fun execute(command: List<String>) {
        TODO("Not yet implemented")
    }
}
