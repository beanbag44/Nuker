package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader
import me.beanbag.nuker.events.PacketEvents
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.modules.Module
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting


class ChatHandler {
    private val chatPrefix =
        Text.literal("[").append(Text.literal("Nuker: ").withColor(Formatting.BLUE.colorValue!!)).append(Text.of("]"))
    private val commandPrefix = "&&"

    init {
        PacketEvents.SEND.register({ packet ->
            if (packet !is ChatMessageC2SPacket || !packet.chatMessage.startsWith(commandPrefix)) {
                return@register ActionResult.PASS
            }

            val command = packet.chatMessage.substring(1).lowercase()
            val commandParts = command.split(" ")
            if (commandParts.isEmpty() || commandParts[0] == "help") {
                //TODO print generic help text or help with specific command
            } else if (commandParts.size == 1 || commandParts.size == 2 && (commandParts[1].toBooleanStrictOrNull() != null || commandParts[1] == "list")) {
                val module = getModuleByName(commandParts[0])
                if (module == null) {
                    //TODO print "Module does not exist" and help message
                    return@register ActionResult.FAIL
                }
                if (commandParts[1] == "list") {
                    //TODO print module settings
                    return@register ActionResult.FAIL
                }
                val enabled = if (commandParts.size == 2) commandParts[1].toBoolean() else !module.enabled

                module.enabled = enabled
            } else {
                val module = getModuleByName(commandParts[0])
                if (module == null) {
                    //TODO print "Invalid Module" and help message
                    return@register ActionResult.FAIL
                }

                //TODO Handle settings.
                val setting: Setting<*>? = module.settingGroups.flatMap { it.settings }
                    .firstOrNull { it.getName().replace(" ", "").equals(commandParts[1], ignoreCase = true) }

            }

            return@register ActionResult.FAIL
        })
    }

    private fun getModuleByName(name: String): Module? {
        return Loader.modules.values.firstOrNull { it.name.replace(" ", "").equals(name, ignoreCase = true) }
    }

}