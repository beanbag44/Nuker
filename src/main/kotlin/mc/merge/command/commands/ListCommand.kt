package mc.merge.command.commands


import mc.merge.ModCore
import mc.merge.ModCore.COMMAND_PREFIX
import mc.merge.command.ExecutableClickEvent
import mc.merge.command.ICommand
import mc.merge.command.ICommandArgument
import mc.merge.command.argument.LiteralArgument
import mc.merge.handler.ChatHandler
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ListCommand : ICommand {
    override val helpText: Text
        get() = Text.empty().append(Text.literal("${COMMAND_PREFIX}list").styled {
            it.withClickEvent(ExecutableClickEvent {
                execute(emptyList())
            })
                .withUnderline(true)
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to run this command")))
        }).append(Text.literal(" - Lists modules and if they are on or off").styled { it.withColor(Formatting.GRAY) })

    override val args: List<ICommandArgument> = listOf(LiteralArgument("list"))

    override fun execute(command: List<String>) {
        ChatHandler.printHeader()
        for (loaderModule in ModCore.modules) {
            ChatHandler.sendChatLine(Text.empty().append(Text.literal(loaderModule.name).styled {
                it.withClickEvent(ExecutableClickEvent {
                    ListModuleCommand().execute(listOf(loaderModule.name))
                })
                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, loaderModule.helpText()))
                    .withUnderline(true)
            }).append(Text.literal(" - ").styled { it.withColor(Formatting.GRAY) }).append(loaderModule.enabledText()))
        }
    }
}