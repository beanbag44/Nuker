package mc.merge.rusher_module

import mc.merge.module.settings.*
import mc.merge.module.settings.BlockListSetting
import org.rusherhack.core.setting.BooleanSetting
import org.rusherhack.core.setting.NullSetting
import org.rusherhack.core.setting.NumberSetting
import org.rusherhack.core.setting.Setting
import org.rusherhack.client.api.setting.ColorSetting as RusherColorSetting
import java.util.function.Consumer

class RusherSettingBuilder {
    fun toRusherSetting(setting:AbstractSetting<*>) : Setting<*> {
        if (setting is BlockListSetting) {
            val rhSetting = NullSetting(setting.getName(), setting.getDescription())

            rhSetting.setVisibility { setting.isVisible() }

            return rhSetting
        } else if (setting is BoolSetting) {
            val rhSetting = BooleanSetting(setting.getName(), setting.getDescription(), setting.getValue())

            rhSetting.setVisibility { setting.isVisible() }
            rhSetting.onChange{value -> setting.setValue(value)}
            setting.getOnChange().add(Consumer{ value -> rhSetting.value = value})

            return rhSetting
        } else if (setting is ColorSetting) {
            val rhSetting = RusherColorSetting(setting.getName(), setting.getDescription(), setting.getValue())

            rhSetting.setVisibility { setting.isVisible() }
            rhSetting.onChange{value -> setting.setValue(value)}
            setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

            return rhSetting
        } else if (setting is DoubleSetting) {
            val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue(), setting.min?: 0.0, setting.max?: 100.0)

            rhSetting.setVisibility { setting.isVisible() }
            rhSetting.onChange{value -> setting.setValue(value) }
            setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

            return rhSetting
        } else if (setting is EntityTypeListSetting) {
            val rhSetting = NullSetting(setting.getName(), setting.getDescription())

            rhSetting.setVisibility { setting.isVisible() }

            return rhSetting
        } else if (setting is EnumSetting<*>) {
            val rhSetting = org.rusherhack.core.setting.EnumSetting(
                setting.getName(),
                setting.getDescriptionWithEnum(),
                setting.getValue()
            )
            rhSetting.setVisibility { setting.isVisible() }

            rhSetting.onChange{value -> setting.setValue(value)}
            setting.addOnChange(Consumer{value -> rhSetting.value = value as Enum<*>})

            return rhSetting
        } else if (setting is FloatSetting) {
            val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue(), setting.min?: 0.0f, setting.max?: 100.0f)

            rhSetting.setVisibility { setting.isVisible() }
            rhSetting.onChange{value -> setting.setValue(value) }
            setting.getOnChange().add(Consumer{value -> rhSetting.value = value})

            return rhSetting
        } else if (setting is IntSetting) {
            val rhSetting = NumberSetting(setting.getName(), setting.getDescription(), setting.getValue().toDouble(), setting.min?.toDouble()?: 0.0, setting.max?.toDouble()?: 100.0)

            rhSetting.setVisibility { setting.isVisible() }
            rhSetting.onChange{value -> setting.setValue(value.toInt()) }
            setting.getOnChange().add(Consumer{value -> rhSetting.value = value.toDouble()})

            return rhSetting
        } else if (setting is ItemListSetting) {
            val rhSetting = NullSetting(setting.getName(), setting.getDescription())

            rhSetting.setVisibility { setting.isVisible() }

            return rhSetting
        } else {
            throw IllegalArgumentException("No conversion to rusher setting for type: ${setting.javaClass}")
        }
    }

//    private inline fun <reified T : Enum<T>> toEnumSetting(setting: EnumSetting<T>) : org.rusherhack.core.setting.EnumSetting<T> {
//
//    }
}