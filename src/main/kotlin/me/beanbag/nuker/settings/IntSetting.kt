package me.beanbag.nuker.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier
import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

class IntSetting(
    name: String,
    description: String,
    defaultValue: Int,
    onChanged: MutableList<Consumer<Int>>?,
    visible: Supplier<Boolean>?,
    var min: Int?,
    var max: Int?,
    var sliderMin: Int?,
    var sliderMax: Int?,
    var step: Int?,

) : AbstractSetting<Int>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {

    override fun valueFromString(value: String): Int? = value.toIntOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        //TODO("Setup visible and onChange")
        return NumberSetting(getName(), getDescription(), getValue(), min!!, max!!)
    }

    override fun toMeteorSetting(): MeteorSetting<*> {
        //TODO("Setup visible and onChange")
        val builder = meteordevelopment.meteorclient.settings.IntSetting.Builder()
            .name(getName())
            .defaultValue(getValue())
            .onChanged { value: Int? -> setValue(value!!) }
        if (min != null) builder.min(min!!)
        if (max != null) builder.max(max!!)
        if (sliderMin != null) builder.sliderMin(sliderMin!!)
        if (sliderMax != null) builder.sliderMax(sliderMax!!)
        return builder.build()
    }
}