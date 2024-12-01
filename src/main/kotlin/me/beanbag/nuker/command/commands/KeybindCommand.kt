package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.EnumArgument
import me.beanbag.nuker.command.arguments.LiteralArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class KeybindCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("${COMMAND_PREFIX}keybind [module] [Set | Clear]")
            .append(Text.literal(" - Sets or clears the current keybind for a module").styled {
                it.withColor(Formatting.GRAY)
            })
    override val args: List<ICommandArgument>
        get() = listOf(LiteralArgument("keybind"), ModuleArgument(), EnumArgument(KeybindAction.entries))

    override fun execute(command: List<String>) {
        val module = ModuleArgument().getModule(command[1]) ?: return
        val action = try {
            KeybindAction.valueOf(command[2].lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        } catch (e: IllegalArgumentException) {
            null
        } ?: return
        when (action) {
            KeybindAction.Set -> {
                //TODO Set keybind
            }
            KeybindAction.Clear -> {
                //TODO clear keybind
            }
        }
    }
}

enum class KeybindAction {
    Set, Clear
}