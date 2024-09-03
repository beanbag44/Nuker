package me.beanbag.nuker.settings

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
    var min: Double?,
    var max: Double?,
    var sliderMin: Double?,
    var sliderMax: Double?,
    var step: Double?,

    ) : AbstractSetting<Double>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {
    override fun valueFromString(value: String): Double? = value.toDoubleOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*>? {
        TODO("Not yet implemented")
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        TODO("Not yet implemented")
    }
}