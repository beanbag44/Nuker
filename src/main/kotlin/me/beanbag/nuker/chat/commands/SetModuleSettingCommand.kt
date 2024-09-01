package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.chat.ChatHandler.COMMAND_PREFIX
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.ModuleSettingValueArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SetModuleSettingCommand: ICommand {
    override val helpText: Text
        get() = Text.literal("$COMMAND_PREFIX[module] [setting] [value]").append(Text.literal(" - Sets a setting to a value").styled { it.withColor(
            Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(ModuleSettingValueArgument())

    override fun execute(command: List<String>) {
        TODO("Not yet implemented")
    }
}