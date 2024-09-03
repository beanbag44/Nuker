package me.beanbag.nuker.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier

import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

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

    override fun toRusherSetting(): RusherSetting<*> {
        //TODO("Setup visible and onChange")
        return NumberSetting(getName(), getDescription(), getValue(), min?: 0, max?: 100)
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        //TODO("Setup visible and onChange")
        val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
            .name(getName())
            .defaultValue(getValue().toDouble())
            .onChanged { value: Double? -> setValue(value!!.toFloat()) }
        if (min != null) builder.min(min!!.toDouble())
        if (max != null) builder.max(max!!.toDouble())
        if (sliderMin != null) builder.sliderMin(sliderMin!!.toDouble())
        if (sliderMax != null) builder.sliderMax(sliderMax!!.toDouble())
        return builder.build()
    }
}