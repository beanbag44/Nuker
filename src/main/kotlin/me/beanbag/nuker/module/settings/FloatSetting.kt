package me.beanbag.nuker.module.settings

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
    private var min: Float?,
    private var max: Float?,
    private var sliderMin: Float?,
    private var sliderMax: Float?,
    private var step: Float?,
) : AbstractSetting<Float>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {

    override fun valueFromString(value: String): Float? = value.toFloatOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = NumberSetting(getName(), getDescription(), getValue(), min?: 0.0f, max?: 100.0f)

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange{value -> setValue(value!!)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getValue().toDouble())
            .onChanged { value: Double? -> setValue(value!!.toFloat()) }
            .visible { isVisible() }
        if (min != null) builder.min(min!!.toDouble())
        if (max != null) builder.max(max!!.toDouble())
        if (sliderMin != null) builder.sliderMin(sliderMin!!.toDouble())
        if (sliderMax != null) builder.sliderMax(sliderMax!!.toDouble())
        if (step != null) builder.decimalPlaces(decimalPlacesFromNum(step!!))

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value.toDouble())})

        return meteorSetting
    }

    private fun decimalPlacesFromNum(number: Float): Int {
        val toString = number.toString()
        if (toString.contains(".")) {
            return toString.replaceBefore(".", "").replace(".", "").length
        }
        return 0
    }
}