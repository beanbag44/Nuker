package me.beanbag.nuker.chat.commands;

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.ModConfigs.modColor
import me.beanbag.nuker.chat.ChatHandler
import me.beanbag.nuker.chat.ChatHandler.toCamelCaseName
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.LiteralArgument
import me.beanbag.nuker.chat.command.arguments.ModuleArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ListModuleCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}list [module]").append(Text.literal(" - Lists module settings and their current value").styled { it.withColor(
            Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(LiteralArgument("list"), ModuleArgument())

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        val module = ModuleArgument().getModule(command[1])!!


        val text = Text.literal("${toCamelCaseName(module.name)} - ").append(module.enabledText()).append(Text.literal("\n"))

        for(settingGroup in module.settingGroups) {
            text.append(Text.literal(settingGroup.name).styled { it.withColor(modColor) })
            for(setting in settingGroup.settings) {
                text.append(Text.literal(" " + toCamelCaseName(setting.getName()))).append(Text.literal(" - ${"TODO, get setting value"}\n").styled { it.withColor(Formatting.GRAY) })
            }
        }
    }
}