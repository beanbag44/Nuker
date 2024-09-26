package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.ModConfigs.modColor
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import me.beanbag.nuker.handlers.ChatHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ListModuleCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}list [module]")
            .append(Text.literal(" - Lists module settings and their current value").styled {
                it.withColor(Formatting.GRAY)
            })

    override val args: List<ICommandArgument> = listOf(LiteralArgument("list"), ModuleArgument())

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        val module = ModuleArgument().getModule(command[1])?: return


        val text =
            Text.literal("${ChatHandler.toCamelCaseName(module.name)} - ").append(module.enabledText()).append(Text.literal("\n"))

        for (settingGroup in module.settingGroups) {
            text.append(Text.literal("${settingGroup.name}\n").styled { it.withColor(modColor) })
            for (setting in settingGroup.settings) {
                text.append(Text.literal(" " + ChatHandler.toCamelCaseName(setting.getName())))
                    .append(Text.literal(" - ${setting.valueToString()}\n").styled { it.withColor(Formatting.GRAY) })
            }
        }
        ChatHandler.sendChatLine(text)
    }
}