package me.beanbag.nuker.command.commands

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.command.ICommand
import me.beanbag.nuker.command.ICommandArgument
import me.beanbag.nuker.command.arguments.ModuleArgument
import me.beanbag.nuker.handlers.ChatHandler
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ToggleModuleCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("$COMMAND_PREFIX[module]").append(Text.literal(" - Toggles a module on or off").styled { it.withColor(
            Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(ModuleArgument())

    override fun execute(command: List<String>) {
        val module = ModuleArgument().getModule(command[0])!!
        module.enabled = !module.enabled
        ChatHandler.sendChatLine(Text.literal("${module.name} is now ").append(module.enabledText()))
    }
}