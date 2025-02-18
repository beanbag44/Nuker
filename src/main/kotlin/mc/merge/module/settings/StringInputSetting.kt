package mc.merge.module.settings

import java.util.function.Consumer
import java.util.function.Supplier


class StringInputSetting(
    name: String,
    description: String,
    defaultValue: String = "",
    onChanged: MutableList<Consumer<String>>?,
    visible: Supplier<Boolean>?,
) : AbstractSetting<String>(
    name,
    description,
    defaultValue,
    onChanged,
    visible ?: Supplier { true }
) {
    override fun valueFromString(value: String): String {
        return value
    }

    override fun valueToString(): String {
        return getValue()
    }

    override fun possibleValues(): List<String>? {
        return null
    }
}