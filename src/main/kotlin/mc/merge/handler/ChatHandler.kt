package mc.merge.handler

import mc.merge.ModCore.COMMAND_PREFIX
import mc.merge.ModCore.MOD_NAME
import mc.merge.ModCore.commands
import mc.merge.ModCore.mc
import mc.merge.ModCore.modColor
import mc.merge.event.events.PacketEvent
import mc.merge.event.onEvent
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.text.Text

object ChatHandler  {
    init {
        onEvent<PacketEvent.Send.Pre> { event ->
            val packet = event.packet

            if (packet !is ChatMessageC2SPacket || !packet.chatMessage.startsWith(COMMAND_PREFIX)) {
                return@onEvent
            }
            val commandParts = packet.chatMessage.substring(COMMAND_PREFIX.length).lowercase().split(" ")
            val command = commands.firstOrNull {
                it.isMatch(commandParts)
            }
            if (command != null) {
                command.execute(packet.chatMessage.substring(COMMAND_PREFIX.length).split(" ").filter { it.isNotBlank() })
                mc.commandHistoryManager.add(packet.chatMessage.substring(COMMAND_PREFIX.length))
            }

            event.cancel()
            return@onEvent
        }
    }

    fun getSuggestions(userInput: String): List<String> {
        val suggestions: List<String> = commands.flatMap { it.getSuggestions(userInput) }.distinct()
        return suggestions
    }

    fun printHeader() {
        sendChatLine(
            Text.literal("=========== ")
                .append(Text.literal("[").append(Text.literal(MOD_NAME).withColor(modColor)).append(Text.of("] ")))
                .append(Text.literal(" ==========="))
        )
    }

    fun toCamelCaseName(name: String): String {
        return name.split(" ")
            .joinToString("") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    }

    fun sendChatLine(message: String) {
        mc.inGameHud.chatHud.addMessage(Text.of(message))
    }

    fun sendChatLine(message: Text) {
        mc.inGameHud.chatHud.addMessage(message)
    }
}