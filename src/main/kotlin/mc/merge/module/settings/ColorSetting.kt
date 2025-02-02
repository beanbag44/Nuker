package mc.merge.module.settings

import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier

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

    private fun hexToColor(hex: String): Color {
        val hexString = hex.replace("#", "")
        var a = 255 // Default to fully opaque
        val r: Int
        val g: Int
        val b: Int

        if (hexString.length == 8) {
            a = hexString.substring(0, 2).toInt(16)
            r = hexString.substring(2, 4).toInt(16)
            g = hexString.substring(4, 6).toInt(16)
            b = hexString.substring(6, 8).toInt(16)
        } else {
            r = hexString.substring(0, 2).toInt(16)
            g = hexString.substring(2, 4).toInt(16)
            b = hexString.substring(4, 6).toInt(16)
        }

        return Color(r, g, b, a)
    }
}
