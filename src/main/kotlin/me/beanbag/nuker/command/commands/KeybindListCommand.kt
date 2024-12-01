package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.EnumArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class KeybindListCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}keybind list")
            .append(Text.literal(" - List all module keybinds").styled {
                it.withColor(Formatting.GRAY)
            })
    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("keybind"), ModuleArgument(), EnumArgument(KeybindAction.entries))

    override fun execute(command: List<String>) {
        for (module in ModConfigs.modules) {
//            if (module.keybind != null) {
//                //TODO List keybinds
//            }
        }
    }
}