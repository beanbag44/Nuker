package me.beanbag.nuker.handlers

import me.beanbag.nuker.ModConfigs.COMMAND_PREFIX
import me.beanbag.nuker.ModConfigs.MOD_NAME
import me.beanbag.nuker.ModConfigs.commands
import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.ModConfigs.modColor
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.PacketEvent
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.text.Text

object ChatHandler : IHandler {
    override var priority = 0
    override var currentlyBeingUsedBy: Module? = null

    init {

        EventBus.subscribe<PacketEvent.Send.Pre>(this) { event ->
            val packet = event.packet

            if (packet !is ChatMessageC2SPacket || !packet.chatMessage.startsWith(COMMAND_PREFIX)) {
                return@subscribe
            }
            val commandParts = packet.chatMessage.substring(COMMAND_PREFIX.length).lowercase().split(" ")
            commands.firstOrNull {
                it.isMatch(commandParts)
            }?.execute(packet.chatMessage.substring(COMMAND_PREFIX.length).split(" ").filter { it.isNotBlank() })

            event.cancel()
            return@subscribe
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