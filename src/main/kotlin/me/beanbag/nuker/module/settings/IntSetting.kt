package me.beanbag.nuker.module.settings

import org.rusherhack.core.setting.NumberSetting
import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class IntSetting(
    name: String,
    description: String,
    defaultValue: Int,
    onChanged: MutableList<Consumer<Int>>?,
    visible: Supplier<Boolean>?,
    private var min: Int?,
    private var max: Int?,
    private var sliderMin: Int?,
    private var sliderMax: Int?,

    ) : AbstractSetting<Int>(name, description, defaultValue, onChanged, visible ?: Supplier { true }) {

    override fun valueFromString(value: String): Int? = value.toIntOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String>? = null

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = NumberSetting(getName(), getDescription(), getValue(), min?: 0, max?: 100)

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange{value -> setValue(value)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*> {
        val builder = meteordevelopment.meteorclient.settings.IntSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value -> setValue(value) }
            .visible { isVisible() }
        min?.let { builder.min(it) }
        max?.let { builder.max(it) }
        sliderMin?.let { builder.sliderMin(it) }
        sliderMax?.let { builder.sliderMax(it) }

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value)})

        return meteorSetting
    }
}