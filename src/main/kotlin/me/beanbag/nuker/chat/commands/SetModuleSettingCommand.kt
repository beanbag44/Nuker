package me.beanbag.nuker.chat.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.chat.ChatHandler.sendChatLine
import me.beanbag.nuker.chat.ChatHandler.toCamelCaseName
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.ModuleArgument
import me.beanbag.nuker.chat.command.arguments.ModuleSettingArgument
import me.beanbag.nuker.chat.command.arguments.ModuleSettingValueArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SetModuleSettingCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("$COMMAND_PREFIX[module] [setting] [value]")
            .append(Text.literal(" - Sets a setting to a value").styled {
                it.withColor(Formatting.GRAY)
            })

    override val args: List<ICommandArgument> = listOf(ModuleSettingValueArgument())

    override fun execute(command: List<String>) {
        val module = ModuleArgument().getModule(command[0])!!
        val setting = ModuleSettingArgument().getSetting(module, command[1])!!
        val value = setting.valueFromString(command[2])
        if (value != null) {
            setting.setValueFromString(command[2])
            sendChatLine(
                Text.literal("Set ${toCamelCaseName(module.name)} - ${toCamelCaseName(setting.getName())} to ${setting.valueToString()}")
                    .styled { it.withColor(Formatting.GREEN) })
        } else {
            sendChatLine(Text.literal("Invalid value").styled { it.withColor(Formatting.RED) })
        }
    }
}