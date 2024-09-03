package me.beanbag.nuker.settings

import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier
import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

class ColorSetting(
    name: String,
    description: String,
    defaultValue: Color,
    onChanged: MutableList<Consumer<Color>>?,
    visible: Supplier<Boolean>
) : AbstractSetting<Color>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): Color? {
        return try {
            Color.decode(value)
        } catch (e: NumberFormatException) {
            null
        }
    }

    override fun valueToString(): String {
        return String.format("#%02x%02x%02x%02x", getValue().alpha ,getValue().red, getValue().green, getValue().blue)
    }

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        TODO()
    }

    override fun toMeteorSetting(): MeteorSetting<*> {
        TODO()
    }
}
