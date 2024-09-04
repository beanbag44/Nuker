package me.beanbag.nuker.module.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier
import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

class DoubleSetting(
    name: String,
    description: String,
    defaultValue: Double,
    onChanged: MutableList<Consumer<Double>>?,
    visible: Supplier<Boolean>?,
    private var min: Double?,
    private var max: Double?,
    private var sliderMin: Double?,
    private var sliderMax: Double?,
    private var step: Double?,

    ) : AbstractSetting<Double>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {
    override fun valueFromString(value: String): Double? = value.toDoubleOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = NumberSetting(getName(), getDescription(), getValue(), min?: 0.0, max?: 100.0)

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange{value -> setValue(value!!)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getValue())
            .onChanged { value: Double? -> setValue(value!!) }
            .visible { isVisible() }
        if (min != null) builder.min(min!!.toDouble())
        if (max != null) builder.max(max!!.toDouble())
        if (sliderMin != null) builder.sliderMin(sliderMin!!.toDouble())
        if (sliderMax != null) builder.sliderMax(sliderMax!!.toDouble())
        if (step != null) builder.decimalPlaces(decimalPlacesFromNum(step!!))

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value)})

        return meteorSetting
    }

    private fun decimalPlacesFromNum(number: Double): Int {
        val toString = number.toString()
        if (toString.contains(".")) {
            return toString.replaceBefore(".", "").replace(".", "").length
        }
        return 0
    }
}