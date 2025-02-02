package mc.merge.module.settings

import java.util.function.Consumer
import java.util.function.Supplier

class FloatSetting(
    name: String,
    description: String,
    defaultValue: Float,
    onChanged: MutableList<Consumer<Float>>?,
    visible: Supplier<Boolean>?,
    var min: Float?,
    var max: Float?,
    var sliderMin: Float?,
    var sliderMax: Float?,
    var step: Float?,
) : AbstractSetting<Float>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {

    override fun valueFromString(value: String): Float? = value.toFloatOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null
}