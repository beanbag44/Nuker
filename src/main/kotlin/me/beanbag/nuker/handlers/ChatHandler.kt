package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader
import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.events.PacketEvents
import me.beanbag.nuker.modules.Module
import me.beanbag.nuker.settings.Setting
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting


class ChatHandler {
    companion object {
        private val modColor = Formatting.BLUE.colorValue!!
        private const val MOD_NAME = "Nuker"
        private const val COMMAND_PREFIX = "&&"
    }

    private val chatPrefix =
        Text.literal("[").append(Text.literal(MOD_NAME).withColor(modColor)).append(Text.of("] "))

    init {
        PacketEvents.SEND.register({ packet ->
            if (packet !is ChatMessageC2SPacket || !packet.chatMessage.startsWith(COMMAND_PREFIX)) {
                return@register ActionResult.PASS
            }

            val command = packet.chatMessage.substring(COMMAND_PREFIX.length).lowercase()
            val parts = command.split(" ").filter { it.isNotBlank() }

            if (parts.isEmpty() || parts[0] == "help") {
                val module = if (parts.size > 1) getModuleByName(parts[1]) else null
                val setting = if (parts.size > 2 && module != null) getSettingByName(module, parts[2]) else null
                if (parts.isEmpty() || parts.size == 1) {
                    printHeader()
                    printMainHelp()
                } else if (
                    parts.size == 2 && (
                        parts[1].equals("modules", ignoreCase = true) ||
                        parts[1].equals("module", ignoreCase = true)
                    )
                ) {
                    printHeader()
                    sendChatLine(Text.of("Click on a module or type \"${COMMAND_PREFIX}help [module]\" for more details"))
                    for (loaderModule in Loader.modules.values) {
                        sendChatLine(
                            Text.empty().append(Text.literal(toCamelCaseName(loaderModule.name)).styled {
                                it.withClickEvent(ExecutableClickEvent {
                                    printHeader()
                                    sendChatLine(moduleHelpText(loaderModule))
                                })
                                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, moduleHelpText(loaderModule)))
                                .withUnderline(true)
                            }).append(Text.literal(" - ${loaderModule.description}").styled { it.withColor(Formatting.GRAY) })
                        )
                    }
                } else if (parts.size == 2 && module != null) {
                    printHeader()
                    sendChatLine(moduleHelpText(module))
                } else if (parts.size == 3 && setting != null) {
                    printHeader()
                    sendChatLine(Text.of("TODO print setting help"))
                    //TODO print setting help
                } else {
                    printHeader()
                    sendChatLine(Text.of("Invalid command"))
                    printMainHelp()
                }
            } else if (parts[0] == "list") {
                val module = if (parts.size > 1) getModuleByName(parts[1]) else null
                if (parts.size == 1) {
                    printHeader()
                    //generic list
                    printHeader()
                    for (loaderModule in Loader.modules.values) {
                        sendChatLine(Text.empty().append(Text.literal(loaderModule.name).styled {
                            it.withClickEvent(ExecutableClickEvent {
                                printHeader()
                                sendChatLine(moduleListText(loaderModule))
                            })
                            .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, moduleListText(loaderModule)))
                            .withUnderline(true)
                        }).append(Text.literal(" - ").styled { it.withColor(Formatting.GRAY) }).append(enabledText(loaderModule.enabled)))
                    }
                } else if (parts.size == 2 && module != null) {
                    printHeader()
                    sendChatLine(moduleListText(module))
                } else {
                    sendChatLine(Text.of("Invalid command"))
                    printMainHelp()
                }
            } else if (parts.size == 1 && getModuleByName(parts[0]) != null) {
                printHeader()
                val module = getModuleByName(parts[0])!!
                toggleModule(module)
            } else if (parts.size == 3 && getModuleByName(parts[0]) != null) {

                val module = getModuleByName(parts[0])!!
                val setting = getSettingByName(module, parts[1])
                if (setting == null) {
                    sendChatLine(Text.of("Invalid setting"))
                    printMainHelp()
                    return@register ActionResult.FAIL
                }
                printHeader()
                sendChatLine(Text.of("TODO set the setting and print its new value"))
                //TODO set the setting and print its new value

            }

            return@register ActionResult.FAIL
        })
    }

    private fun getModuleByName(name: String): Module? {
        return Loader.modules.values.firstOrNull { it.name.replace(" ", "").equals(name, ignoreCase = true) }
    }

    private fun getSettingByName(module: Module, name: String): Setting<*>? {
        return module.settingGroups.flatMap { it.settings }
            .firstOrNull { it.getName().replace(" ", "").equals(name, ignoreCase = true) }
    }

    private fun sendChatLine(message: Text) {
        mc.inGameHud.chatHud.addMessage(message)
//        mc.inGameHud.chatHud.addMessage(Text.empty().append(chatPrefix).append(message))
    }

    private fun toCamelCaseName(name: String): String {
        return name.split(" ")
            .joinToString("") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    }

    private fun enabledText(enabled: Boolean): Text {
        return Text.literal(if (enabled) "ON" else "OFF")
            .styled { if (enabled) it.withColor(Formatting.GREEN) else it.withColor(Formatting.RED) }
    }


    private fun printHeader() {
        sendChatLine(Text.literal("=========== ").append(chatPrefix).append(Text.literal(" ===========")))
    }

    private fun printMainHelp() {
        sendChatLine(Text.of("Hovering over modules/settings will show more details.\n"))
        sendChatLine(Text.of("Available commands:\n"))
        sendChatLine(Text.literal("${COMMAND_PREFIX}help").append(Text.literal(" - Shows this list").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}help modules").append(Text.literal(" - Lists modules and their descriptions").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}help [module]").append(Text.literal(" - Lists module settings and their descriptions").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}help [module] [setting]").append(Text.literal(" - Lists details about the setting").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}list").append(Text.literal(" - Lists modules and if they are on or off").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}list [module]").append(Text.literal(" - Lists module settings and their current value").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}[module]").append(Text.literal(" - Toggles a module on or off").styled { it.withColor(Formatting.GRAY) }))
        sendChatLine(Text.literal("${COMMAND_PREFIX}[module] [setting] [value]").append(Text.literal(" - Sets a setting to a value").styled { it.withColor(Formatting.GRAY) }))
    }

    private fun moduleHelpText(module: Module) : Text {
        val text = Text.literal("${toCamelCaseName(module.name)} - ${module.description}\n")

        for(settingGroup in module.settingGroups) {
            text.append(Text.literal(settingGroup.name + "\n").styled { it.withColor(modColor) }).append(Text.literal(" - ${settingGroup.description}\n").styled { it.withColor(Formatting.GRAY) })
            for(setting in settingGroup.settings) {
                text.append(Text.literal(" " + toCamelCaseName(setting.getName()))).append(Text.literal(" - ${setting.getDescription()}\n").styled { it.withColor(Formatting.GRAY) })
            }
        }
        return text
    }

    private fun moduleListText(module: Module) : Text {
        val text = Text.literal("${toCamelCaseName(module.name)} - ").append(enabledText(module.enabled)).append(Text.literal("\n"))

        for(settingGroup in module.settingGroups) {
            text.append(Text.literal(settingGroup.name).styled { it.withColor(modColor) })
            for(setting in settingGroup.settings) {
                text.append(Text.literal(" " + toCamelCaseName(setting.getName()))).append(Text.literal(" - ${"TODO, get setting value"}\n").styled { it.withColor(Formatting.GRAY) })
            }
        }
        return text
    }
    private fun toggleModule(module: Module) {
        module.enabled = !module.enabled
        sendChatLine(Text.literal("${module.name} is now ").append(enabledText(module.enabled)))
    }
}

open class ExecutableClickEvent(val onClick: () -> Unit) : ClickEvent(Action.RUN_COMMAND, "Will not be used")