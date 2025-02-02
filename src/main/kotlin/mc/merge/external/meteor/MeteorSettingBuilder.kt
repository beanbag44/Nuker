package mc.merge.external.meteor

import mc.merge.module.settings.*
import mc.merge.module.settings.BlockListSetting
import meteordevelopment.meteorclient.settings.Setting
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import java.awt.Color
import java.util.function.Consumer

class MeteorSettingBuilder {
    fun toMeteorSetting(setting: AbstractSetting<*>) : Setting<*> {
        return when (setting) {
            is BlockListSetting -> {
                val builder = meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue())
                    .onChanged { value: List<Block> -> setting.setValue(value) }
                    .visible { setting.isVisible() }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value) })

                meteorSetting
            }

            is BoolSetting -> {
                val builder = meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue())
                    .onChanged { value -> setting.setValue(value) }
                    .visible { setting.isVisible() }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value) })

                meteorSetting
            }

            is ColorSetting -> {
                val builder = meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(
                        SettingColor(
                            setting.getDefaultValue().red,
                            setting.getDefaultValue().green,
                            setting.getDefaultValue().blue,
                            setting.getDefaultValue().alpha
                        )
                    )
                    .onChanged { value: SettingColor -> setting.setValue(Color(value.r, value.g, value.b, value.a)) }
                    .visible { setting.isVisible() }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value ->
                    meteorSetting.set(
                        SettingColor(
                            value.red,
                            value.green,
                            value.blue,
                            value.alpha
                        )
                    )
                })

                meteorSetting
            }

            is DoubleSetting -> {
                val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue())
                    .onChanged { value -> setting.setValue(value) }
                    .visible { setting.isVisible() }
                setting.min?.let { builder.min(it) }
                setting.max?.let { builder.max(it) }
                setting.sliderMin?.let { builder.sliderMin(it) }
                setting.sliderMax?.let { builder.sliderMax(it) }
                setting.step?.let { builder.decimalPlaces(decimalPlacesFromNum(it)) }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value) })

                meteorSetting
            }
            is EntityTypeListSetting -> {
                val builder = meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue().toSet())
                    .onChanged { value: Set<EntityType<*>> -> setting.setValue(value.toList()) }
                    .visible { setting.isVisible() }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value.toSet()) })

                meteorSetting
            }
            is mc.merge.module.settings.EnumSetting<*> -> {
                setting.toMeteorSetting()
            }
            is FloatSetting -> {
                val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue().toDouble())
                    .onChanged { value -> setting.setValue(value.toFloat()) }
                    .visible { setting.isVisible() }
                setting.min?.let { builder.min(it.toDouble()) }
                setting.max?.let { builder.max(it.toDouble()) }
                setting.sliderMin?.let { builder.sliderMin(it.toDouble()) }
                setting.sliderMax?.let { builder.sliderMax(it.toDouble()) }
                setting.step?.let { builder.decimalPlaces(decimalPlacesFromNum(it)) }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value.toDouble()) })

                meteorSetting
            }
            is IntSetting -> {
                val builder = meteordevelopment.meteorclient.settings.IntSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue())
                    .onChanged { value -> setting.setValue(value) }
                    .visible { setting.isVisible() }
                setting.min?.let { builder.min(it) }
                setting.max?.let { builder.max(it) }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value) })

                meteorSetting
            }
            is ItemListSetting -> {
                val builder = meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                    .name(setting.getName())
                    .description(setting.getDescription())
                    .defaultValue(setting.getDefaultValue())
                    .onChanged { value -> setting.setValue(value) }
                    .visible { setting.isVisible() }

                val meteorSetting = builder.build()
                setting.getOnChange().add(Consumer { value -> meteorSetting.set(value) })

                meteorSetting
            }
            else -> {
                throw IllegalArgumentException("Unsupported setting type: ${setting::class.simpleName}")
            }
        }
    }

    private fun decimalPlacesFromNum(number: Number): Int {
        val toString = number.toString()
        if (toString.contains(".")) {
            return toString.replaceBefore(".", "").replace(".", "").length
        }
        return 0
    }

}