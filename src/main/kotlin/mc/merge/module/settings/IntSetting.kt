package mc.merge.module.settings

import java.util.function.Consumer
import java.util.function.Supplier

class IntSetting(
    name: String,
    description: String,
    defaultValue: Int,
    onChanged: MutableList<Consumer<Int>>?,
    visible: Supplier<Boolean>?,
    var min: Int?,
    var max: Int?,
    private var sliderMin: Int?,
    private var sliderMax: Int?,

    ) : AbstractSetting<Int>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {

    override fun valueFromString(value: String): Int? = value.toIntOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

}