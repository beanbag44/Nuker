package mc.merge.rusher_module

import mc.merge.ModCore
import mc.merge.module.settings.*
import mc.merge.module.settings.BlockListSetting
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import org.rusherhack.core.setting.BooleanSetting
import org.rusherhack.core.setting.NullSetting
import org.rusherhack.core.setting.NumberSetting
import org.rusherhack.core.setting.Setting
import org.rusherhack.core.setting.StringSetting
import org.rusherhack.client.api.setting.ColorSetting as RusherColorSetting
import java.util.function.Consumer

class RusherSettingBuilder {
    fun toRusherSetting(setting:AbstractSetting<*>) : Setting<*> {
        when (setting) {
            is BlockListSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val searchSetting = StringSetting("Search", "Search for blocks", "")
                rhSetting.addSubSettings(searchSetting)
                val blockValues = setting.getValue().map { setting.listValueToString(it) }

                setting.allPossibleValues().forEach { block ->
                    BooleanSetting(block, blockValues.contains(block)).apply{
                        setVisibility {
                            setting.isVisible() && (
                                    searchSetting.value.isEmpty() ||
                                            block.contains(searchSetting.value, ignoreCase = true) ||
                                            searchSetting.value.lowercase() == "enabled" && this.value
                                    )
                        }
                        onChange { value ->
                            if (value) {
                                setting.setValue(listOf(setting.getValue(), listOf(setting.listValueFromString(block)!!)).flatten())
                            } else {
                                setting.setValue(setting.getValue().filter { setting.listValueToString(it) != (block) })
                            }
                        }
                        setting.getOnChange().add(Consumer{ value : List<Block> -> this.value = value.any{ it == setting.listValueFromString(this.name)}})
                    }.also { rhSetting.addSubSettings(it) }
                }
                rhSetting.subSettings.sortWith{ a, b ->
                    if (a is StringSetting) {
                        return@sortWith -1
                    } else if (b is StringSetting) {
                        return@sortWith 1
                    }
                    return@sortWith a.name.compareTo(b.name)
                }
                return rhSetting
            }

            is BoolSetting -> {
                val rhSetting = BooleanSetting(setting.getName(), setting.getDescription(), setting.getValue())

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value)}
                setting.getOnChange().add(Consumer{ value -> rhSetting.value = value})

                return rhSetting
            }

            is ColorSetting -> {
                val rhSetting = RusherColorSetting(setting.getName(), setting.getDescription(), setting.getValue())

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value)}
                setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

                return rhSetting
            }

            is DoubleSetting -> {
                val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue(), setting.min?: 0.0, setting.max?: 100.0)

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value) }
                setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

                return rhSetting
            }

            is EntityTypeListSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val searchSetting = StringSetting("Search", "Search for Entities", "")
                rhSetting.addSubSettings(searchSetting)
                val entityValues = setting.getValue().map { setting.listValueToString(it) }

                setting.allPossibleValues().forEach { entity ->
                    BooleanSetting(entity, entityValues.contains(entity)).apply{
                        setVisibility {
                            setting.isVisible() && (
                                    searchSetting.value.isEmpty() ||
                                            entity.contains(searchSetting.value, ignoreCase = true) ||
                                            searchSetting.value.lowercase() == "enabled" && this.value
                                    )
                        }
                        onChange { value ->
                            if (value) {
                                setting.setValue(listOf(setting.getValue(), listOf(setting.listValueFromString(entity)!!)).flatten())
                            } else {
                                setting.setValue(setting.getValue().filter { setting.listValueToString(it) != (entity) })
                            }
                        }
                        setting.getOnChange().add(Consumer{ value : List<EntityType<*>> -> this.value = value.any{ it == setting.listValueFromString(this.name)}})
                    }.also { rhSetting.addSubSettings(it) }
                }
                rhSetting.subSettings.sortWith{ a, b ->
                    if (a is StringSetting) {
                        return@sortWith -1
                    } else if (b is StringSetting) {
                        return@sortWith 1
                    }
                    a.name.compareTo(b.name)
                }


                return rhSetting
            }

            is EnumSetting<*> -> {
                val rhSetting = org.rusherhack.core.setting.EnumSetting(
                    setting.getName(),
                    setting.getDescriptionWithEnum(),
                    setting.getValue()
                )
                rhSetting.setVisibility { setting.isVisible() }

                rhSetting.onChange{value -> setting.setValue(value)}
                setting.addOnChange(Consumer{value -> rhSetting.value = value as Enum<*>})

                return rhSetting
            }

            is FloatSetting -> {
                val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue(), setting.min?: 0.0f, setting.max?: 100.0f)

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value) }
                setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

                return rhSetting
            }

            is IntSetting -> {
                val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue().toDouble(), setting.min?.toDouble()?: 0.0, setting.max?.toDouble()?: 100.0)

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value.toInt()) }
                setting.getOnChange().add(Consumer{value -> rhSetting.value = value.toDouble()})

                return rhSetting
            }

            is ItemListSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val searchSetting = StringSetting("Search", "Search for Items", "")
                rhSetting.addSubSettings(searchSetting)
                val itemValues = setting.getValue().map { setting.listValueToString(it) }

                setting.allPossibleValues().forEach { item ->
                    BooleanSetting(item, itemValues.contains(item)).apply{
                        setVisibility {
                            setting.isVisible() && (
                                    searchSetting.value.isEmpty() ||
                                            item.contains(searchSetting.value, ignoreCase = true) ||
                                            searchSetting.value.lowercase() == "enabled" && this.value
                                    )
                        }
                        onChange { value ->
                            if (value) {
                                setting.setValue(listOf(setting.getValue(), listOf(setting.listValueFromString(item)!!)).flatten())
                            } else {
                                setting.setValue(setting.getValue().filter { setting.listValueToString(it) != (item) })
                            }
                        }
                        setting.getOnChange().add(Consumer{ value : List<Item> -> this.value = value.any{ it == setting.listValueFromString(this.name)}})
                    }.also { rhSetting.addSubSettings(it) }
                }
                rhSetting.subSettings.sortWith{ a, b ->
                    if (a is StringSetting) {
                        return@sortWith -1
                    } else if (b is StringSetting) {
                        return@sortWith 1
                    }
                    a.name.compareTo(b.name)
                }


                return rhSetting
            }

            else -> {
                ModCore.LOGGER.warn("No conversion to rusher setting for type: ${setting.javaClass}")
                return NullSetting(setting.getName(), setting.getDescription())
            }
        }
    }
}