package me.beanbag.nuker.chat.commands;

import me.beanbag.nuker.chat.ChatHandler.COMMAND_PREFIX
import me.beanbag.nuker.chat.ChatHandler.sendChatLine
import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.ICommandArgument
import me.beanbag.nuker.chat.command.arguments.ModuleArgument
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
        sendChatLine(Text.literal("${module.name} is now ").append(module.enabledText()))
    }
}