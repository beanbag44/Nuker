package me.beanbag.nuker.module.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

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
        rhSetting.onChange{value -> setValue(value)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue().toDouble())
            .onChanged { value -> setValue(value.toFloat()) }
            .visible { isVisible() }
        min?.let { builder.min(it.toDouble()) }
        max?.let { builder.max(it.toDouble()) }
        sliderMin?.let { builder.sliderMin(it.toDouble()) }
        sliderMax?.let { builder.sliderMax(it.toDouble()) }
        step?.let { builder.decimalPlaces(decimalPlacesFromNum(it)) }

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