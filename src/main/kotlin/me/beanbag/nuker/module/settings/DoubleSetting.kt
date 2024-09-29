package me.beanbag.nuker.module.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

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
        rhSetting.onChange{value -> setValue(value) }
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value -> setValue(value) }
            .visible { isVisible() }
        min?.let { builder.min(it) }
        max?.let { builder.max(it) }
        sliderMin?.let { builder.sliderMin(it) }
        sliderMax?.let { builder.sliderMax(it) }
        step?.let { builder.decimalPlaces(decimalPlacesFromNum(it)) }

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