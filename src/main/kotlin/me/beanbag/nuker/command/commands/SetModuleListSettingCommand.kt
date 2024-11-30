package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.ModConfigs.modColor
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.ListAction
import me.beanbag.nuker.command.arguments.ModuleArgument
import me.beanbag.nuker.command.arguments.ModuleSettingArgument
import me.beanbag.nuker.command.arguments.ModuleSettingListValueArgument
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.settings.AbstractListSetting
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SetModuleListSettingCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("$COMMAND_PREFIX[module] [setting] [add | remove] [value]")
            .append(Text.literal(" - Modifies a list setting value").styled {
                it.withColor(Formatting.GRAY)
            })
    override val args: List<ICommandArgument>
        get() = listOf(ModuleSettingListValueArgument())

    override fun execute(command: List<String>) {
        val module = ModuleArgument().getModule(command[0]) ?: return
        val setting = ModuleSettingArgument().getSetting(module, command[1]) ?: return
        val listAction = try {
            ListAction.valueOf(command[2].lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        } catch (e: IllegalArgumentException) {
            null
        } ?: return
        if (setting !is AbstractListSetting<*>) {
            return
        }
        val value = setting.listValueFromString(command[3])
        if (value != null) {
            return when (listAction) {
                ListAction.Add -> {
                    setting.addFromString(command[3])
                    ChatHandler.sendChatLine(
                        (Text.literal("[").append(Text.literal(MOD_NAME).withColor(modColor)).append(Text.of("] "))
                            .append(
                                Text.literal("${module.name} - ${setting.getName()}: ")
                                    .withColor(Formatting.GRAY.colorValue!!)
                            ).append(setting.valueToString()))
                    )
                }

                ListAction.Remove -> {
                    setting.removeFromString(command[3])
                    ChatHandler.sendChatLine(
                        (Text.literal("[").append(Text.literal(MOD_NAME).withColor(modColor)).append(Text.of("] "))
                            .append(
                                Text.literal("${module.name} - ${setting.getName()}: ")
                                    .withColor(Formatting.GRAY.colorValue!!)
                            ).append(setting.valueToString()))
                    )
                }
            }
        }
    }
}