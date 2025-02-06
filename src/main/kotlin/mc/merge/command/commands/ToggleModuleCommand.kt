package mc.merge.command.commands

import mc.merge.command.ICommand
import mc.merge.command.ICommandArgument
import mc.merge.command.argument.ModuleArgument
import mc.merge.handler.ChatHandler
import mc.merge.ModCore.commandPrefix
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ToggleModuleCommand : ICommand {
    override val helpText: Text
        get() = Text.literal("$commandPrefix[module]").append(Text.literal(" - Toggles a module on or off").styled { it.withColor(
            Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(ModuleArgument())

    override fun execute(command: List<String>) {
        val module = ModuleArgument().getModule(command[0]) ?: return
        module.enabled = !module.enabled
        ChatHandler.sendChatLine(Text.literal("${module.name} is now ").append(module.enabledText()))
    }
}