package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import me.beanbag.nuker.command.arguments.ModuleSettingArgument
import me.beanbag.nuker.command.arguments.ModuleSettingValueArgument
import me.beanbag.nuker.handlers.ChatHandler
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
        val module = ModuleArgument().getModule(command[0]) ?: return
        val setting = ModuleSettingArgument().getSetting(module, command[1]) ?: return
        val value = setting.valueFromString(command[2])
        if (value != null) {
            setting.setValueFromString(command[2])
            ChatHandler.sendChatLine(
                Text.literal("Set ${ChatHandler.toCamelCaseName(module.name)} - ${ChatHandler.toCamelCaseName(setting.getName())} to ${setting.valueToString()}")
                    .styled { it.withColor(Formatting.GREEN) })
        } else {
            ChatHandler.sendChatLine(Text.literal("Invalid value").styled { it.withColor(Formatting.RED) })
        }
    }
}