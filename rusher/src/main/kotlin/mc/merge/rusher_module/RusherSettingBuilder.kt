package mc.merge.rusher_module

import mc.merge.ModCore
import mc.merge.module.settings.*
import mc.merge.module.settings.BlockListSetting
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
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
                                val blockValue = setting.listValueFromString(block) ?: return@onChange
                                setting.setValue(listOf(setting.getValue(), listOf(blockValue)).flatten())
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

            is BlockPosSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val xSetting = NumberSetting("X", "X Coordinate", setting.getValue().x.toDouble(), -30000000.0, 30000000.0)
                val ySetting = NumberSetting("Y", "Y Coordinate", setting.getValue().y.toDouble(), -30000000.0, 30000000.0)
                val zSetting = NumberSetting("Z", "Z Coordinate", setting.getValue().z.toDouble(), -30000000.0, 30000000.0)

                xSetting.onChange { value -> setting.setValue(BlockPos(value.toInt(), setting.getValue().y, setting.getValue().z)) }
                ySetting.onChange { value -> setting.setValue(BlockPos(setting.getValue().x, value.toInt(), setting.getValue().z)) }
                zSetting.onChange { value -> setting.setValue(BlockPos(setting.getValue().x, setting.getValue().y, value.toInt())) }

                setting.getOnChange().add(Consumer{ value -> xSetting.value = value.x.toDouble() })
                setting.getOnChange().add(Consumer{ value -> ySetting.value = value.y.toDouble() })
                setting.getOnChange().add(Consumer{ value -> zSetting.value = value.z.toDouble() })

                rhSetting.addSubSettings(xSetting)
                rhSetting.addSubSettings(ySetting)
                rhSetting.addSubSettings(zSetting)

                return rhSetting
            }

            is BlockSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val searchSetting = StringSetting("Search", "Search for blocks", "")
                rhSetting.addSubSettings(searchSetting)
                val blockValue = setting.valueToString()

                setting.possibleValues().forEach { block ->
                    BooleanSetting(block, blockValue == block).apply{
                        setVisibility {
                            setting.isVisible() && (
                                    searchSetting.value.isEmpty() ||
                                            block.contains(searchSetting.value, ignoreCase = true) ||
                                            searchSetting.value.lowercase() == "enabled" && this.value
                                    )
                        }
                        onChange { value ->
                            if (value) {
                                setting.setValue(setting.valueFromString(block))
                            } else {
                                setting.setValue(Blocks.AIR)
                            }
                        }

                        setting.getOnChange().add(Consumer { value: Block ->
                            this.value = Registries.BLOCK.getId(value).toString()
                                .apply { replace("minecraft:", "", true) } == this.name
                        })
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
                                val entityValue = setting.listValueFromString(entity) ?: return@onChange
                                setting.setValue(listOf(setting.getValue(), listOf(entityValue)).flatten())
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
                                val itemValue = setting.listValueFromString(item) ?: return@onChange
                                setting.setValue(listOf(setting.getValue(), listOf(itemValue)).flatten())
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
            is ItemSetting -> {
                val rhSetting = NullSetting(setting.getName(), setting.getDescription())
                val searchSetting = StringSetting("Search", "Search for Items", "")
                rhSetting.addSubSettings(searchSetting)
                val itemValue = setting.valueToString()

                setting.possibleValues().forEach { item ->
                    BooleanSetting(item, itemValue == item).apply{
                        setVisibility {
                            setting.isVisible() && (
                                    searchSetting.value.isEmpty() ||
                                            item.contains(searchSetting.value, ignoreCase = true) ||
                                            searchSetting.value.lowercase() == "enabled" && this.value
                                    )
                        }
                        onChange { value ->
                            if (value) {
                                setting.setValue(setting.valueFromString(item))
                            } else {
                                setting.setValue(Items.AIR)
                            }
                        }

                        setting.getOnChange().add(Consumer { value: Item ->
                            this.value = Registries.ITEM.getId(value).toString()
                                .apply { replace("minecraft:", "", true) } == this.name
                        })
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

            is StringInputSetting -> {
                val rhSetting = StringSetting(setting.getName(), setting.getDescription(), setting.getValue())

                rhSetting.setVisibility { setting.isVisible() }
                rhSetting.onChange{value -> setting.setValue(value) }
                setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

                return rhSetting
            }

            else -> {
                ModCore.LOGGER.warn("No conversion to rusher setting for type: ${setting.javaClass}")
                return NullSetting(setting.getName(), setting.getDescription())
            }
        }
    }
}