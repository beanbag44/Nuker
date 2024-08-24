package me.beanbag.nuker.handlers

import me.beanbag.nuker.Loader
import me.beanbag.nuker.events.PacketEvents
import me.beanbag.nuker.settings.Setting
import me.beanbag.nuker.modules.Module
import net.minecraft.block.Block
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import java.awt.Color


class ChatHandler {
    private val chatPrefix =
        Text.literal("[").append(Text.literal("Nuker: ").withColor(Formatting.BLUE.colorValue!!)).append(Text.of("]"))
    private val commandPrefix = "&&"

    init {
//        PacketEvents.SEND.register({ packet ->
//            if (packet !is ChatMessageC2SPacket || !packet.chatMessage.startsWith(commandPrefix)) {
//                return@register ActionResult.PASS
//            }
//
//            val command = packet.chatMessage.substring(1).lowercase()
//            val commandParts = command.split(" ")
//            if (commandParts.isEmpty() || commandParts[0] == "help") {
//                //TODO print generic help text or help with specific command
//            } else if (commandParts.size == 1 || commandParts.size == 2 && (commandParts[1].toBooleanStrictOrNull() != null || commandParts[1] == "list")) {
//                val module = getModuleByName(commandParts[0])
//                if (module == null) {
//                    //TODO print "Module does not exist" and help message
//                    return@register ActionResult.FAIL
//                }
//                if (commandParts[1] == "list") {
//                    //TODO print module settings
//                    return@register ActionResult.FAIL
//                }
//                val enabled = if (commandParts.size == 2) commandParts[1].toBoolean() else !module.enabled
//
//                module.enabled = enabled
//            } else {
//                val module = getModuleByName(commandParts[0])
//                if (module == null) {
//                    //TODO print "Invalid Module" and help message
//                    return@register ActionResult.FAIL
//                }
//
//                //TODO Handle settings.
//                val setting: Setting<*>? = module.settingGroups.flatMap { it.settings }
//                    .firstOrNull { it.getName().replace(" ", "").equals(commandParts[1], ignoreCase = true) }
//
//            }
//
//            return@register ActionResult.FAIL
//        })
    }

    private fun getModuleByName(name: String): Module? {
        return Loader.modules.values.firstOrNull { it.name.replace(" ", "").equals(name, ignoreCase = true) }
    }

    fun setValue(setting: Setting<*>, value: String) {
        //do the above if statement as a when statement

//        setting.thisIfType<Setting<Int>>()?.setValue(value.toInt())
//        setting.thisIfType<Setting<Float>>()?.setValue(value.toFloat())
//        setting.thisIfType<Setting<Double>>()?.setValue(value.toDouble())
//        setting.thisIfType<Setting<Boolean>>()?.setValue(value.toBoolean())
//        setting.thisIfType<Setting<List<Block>>>()?.setValue(value.split(",").map {
//            BlockArgumentParser.block(
//                mc.world!!.createCommandRegistryWrapper(RegistryKeys.BLOCK),
//                value,
//                false
//            ).blockState.block
//        })
//        setting.thisIfType<Setting<Color>>()?.setValue(Color.decode(value))
//        setting.thisIfType<Setting<Enum<*>>>()?.let { enumSetting ->
//            enumSetting.getValue().javaClass.enumConstants.forEach {constantValue -> if (constantValue.name.equals(value, ignoreCase = true)) enumSetting.setValue(constantValue) }
//        }



//        if (setting.isInstanceOf<Int>()) {
//          (setting as Setting<Int>).setValue(value.toInt())
//        } else if (setting.isInstanceOf<Float>()) {
//            (setting as Setting<Float>).setValue(value.toFloat())
//        } else if (setting.isInstanceOf<Double>()) {
//            (setting as Setting<Double>).setValue(value.toDouble())
//        } else if (setting.isInstanceOf<Boolean>()) {
//            (setting as Setting<Boolean>).setValue(value.toBoolean())
//        } else if (setting.isInstanceOf<List<Block>>()) {
//            (setting as Setting<List<Block>>).setValue(value.split(",").map {
//                BlockArgumentParser.block(
//                    mc.world!!.createCommandRegistryWrapper(RegistryKeys.BLOCK),
//                    value,
//                    false
//                ).blockState.block
//            })
//        } else if (setting.isInstanceOf<Color>()) {
//            (setting as Setting<Color>).setValue(Color.decode(value))
//        } else if (setting.isInstanceOf<Enum<*>>()) {
//            stringToEnum(
//                value,
//                (setting.getValue() as Enum<*>).javaClass.enumConstants.first() as Enum<*>
//            )?.let { (setting as Setting<Enum<*>>).setValue(it) }
//        }
//        when (setting.getValue()) {
//            is Int -> (setting.isInstanceOf<Int>()).setValue(value.toInt())
//            is Float -> (setting as Setting<Float>).setValue(value.toFloat())
//            is Double -> (setting as Setting<Double>).setValue(value.toDouble())
//            is Boolean -> (setting as Setting<Boolean>).setValue(value.toBoolean())
//            is List<*> -> if ((setting.getValue() as List<*>).any { it is Block }) {
//                (setting as Setting<List<Block>>).setValue(value.split(",").map {
//                    BlockArgumentParser.block(
//                        mc.world!!.createCommandRegistryWrapper(RegistryKeys.BLOCK),
//                        value,
//                        false
//                    ).blockState.block
//                })
//            }
//
//            is Color -> (setting as Setting<Color>).setValue(Color.decode(value))
//            is Enum<*> -> stringToEnum(
//                value,
//                (setting.getValue() as Enum<*>).javaClass.enumConstants.first() as Enum<*>
//            )?.let { (setting as Setting<Enum<*>>).setValue(it) }
//        }
    }

    private fun printValue(setting: Setting<*>) {
//        when (setting.getValue()) {
//            is Int -> (setting as Setting<Int>).setValue(value.toInt())
//            is Float -> (setting as Setting<Float>).setValue(value.toFloat())
//            is Double -> (setting as Setting<Double>).setValue(value.toDouble())
//            is Boolean -> (setting as Setting<Boolean>).setValue(value.toBoolean())
//            is List<*> -> if ((setting.getValue() as List<*>).any { it is Block }) {
//                (setting as Setting<List<Block>>).setValue(value.split(",").map {
//                    BlockArgumentParser.block(
//                        mc.world!!.createCommandRegistryWrapper(RegistryKeys.BLOCK),
//                        value,
//                        false
//                    ).blockState.block
//                })
//            }
//
//            is Color -> (setting as Setting<Color>).setValue(Color.decode(value))
//            is Enum<*> -> stringToEnum(
//                value,
//                (setting.getValue() as Enum<*>).javaClass.enumConstants.first() as Enum<*>
//            )?.let { (setting as Setting<Enum<*>>).setValue(it) }
//        }
    }

    private fun <T : Enum<T>> stringToEnum(value: String, enumType: Enum<T>): T? {
        enumType.declaringJavaClass.enumConstants.firstOrNull() { it.name.equals(value, ignoreCase = true) }?.let { return it }
        return null
    }

}