package me.beanbag.nuker.module.settings

import meteordevelopment.meteorclient.utils.render.color.SettingColor
import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.client.api.setting.ColorSetting as RusherColorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class ColorSetting(
    name: String,
    description: String,
    defaultValue: Color,
    onChanged: MutableList<Consumer<Color>>?,
    visible: Supplier<Boolean>
) : AbstractSetting<Color>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): Color? {
        return try {
            hexToColor(value)
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun valueToString(): String {
        return String.format("#%02x%02x%02x%02x", getValue().alpha ,getValue().red, getValue().green, getValue().blue)
    }

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = RusherColorSetting(getName(), getDescription(), getValue())

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange{value -> setValue(value)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*> {
        val builder = meteordevelopment.meteorclient.settings.ColorSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(SettingColor(getDefaultValue().red, getDefaultValue().green, getDefaultValue().blue, getDefaultValue().alpha))
            .onChanged { value: SettingColor -> setValue(Color(value.r, value.g, value.b, value.a)) }
            .visible { isVisible() }

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(SettingColor(value.red, value.green, value.blue, value.alpha))})

        return meteorSetting
    }

    fun hexToColor(hex: String): Color {
        var hex = hex
        if (hex.startsWith("#")) {
            hex = hex.substring(1)
        }

        var a = 255 // Default to fully opaque
        val r: Int
        val g: Int
        val b: Int

        if (hex.length == 8) {
            a = hex.substring(0, 2).toInt(16)
            r = hex.substring(2, 4).toInt(16)
            g = hex.substring(4, 6).toInt(16)
            b = hex.substring(6, 8).toInt(16)
        } else {
            r = hex.substring(0, 2).toInt(16)
            g = hex.substring(2, 4).toInt(16)
            b = hex.substring(4, 6).toInt(16)
        }

        return Color(r, g, b, a)
    }
}
