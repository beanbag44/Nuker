package me.beanbag.nuker.settings

import meteordevelopment.meteorclient.settings.BoolSetting
import meteordevelopment.meteorclient.settings.ColorSetting
import meteordevelopment.meteorclient.settings.DoubleSetting
import meteordevelopment.meteorclient.settings.EnumSetting
import meteordevelopment.meteorclient.settings.IntSetting
import meteordevelopment.meteorclient.settings.Setting
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder
import org.rusherhack.core.setting.BooleanSetting
import org.rusherhack.core.setting.NumberSetting
import java.awt.Color
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty
import org.rusherhack.core.setting.Setting as RusherSetting
import org.rusherhack.client.api.setting.ColorSetting as RusherColorSetting
import org.rusherhack.core.setting.EnumSetting as RusherEnumSetting

class Setting<T: Any> {

    private var name: String
    private var description: String
    private var value: T
    private lateinit var min: T
    private lateinit var max: T
    private lateinit var sliderMin: T
    private lateinit var sliderMax: T
    private lateinit var step: T
    private var onChange: List<Consumer<T>>? = null
    private var visible: Supplier<Boolean>

    constructor(
        name: String, description: String,
        value: T, min: T, max: T, sliderMin: T, sliderMax: T, step: T,
        onChange: List<Consumer<T>>?,
        visible: Supplier<Boolean>
    ) {
        this.name = name
        this.description = description
        this.value = value
        this.min = min
        this.max = max
        this.sliderMin = sliderMin
        this.sliderMax = sliderMax
        this.step = step
        this.onChange = onChange
        this.visible = visible
    }

    constructor(
        name: String, description: String,
        value: T,
        onChange: List<Consumer<T>>?,
        visible: Supplier<Boolean>
    ) {
        this.name = name
        this.description = description
        this.value = value
        this.onChange = onChange
        this.visible = visible
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        onChange?.forEach { it.accept(value) }
    }

    fun setValue(value: T) {
        this.value = value
        onChange?.forEach { it.accept(value) }
    }

    fun getName() =
        name

    fun getDescription() =
        description

    fun getMin() =
        min

    fun getMax() =
        max

    fun getSliderMin() =
        sliderMin

    fun getSliderMax() =
        sliderMax

    fun getStep() =
        step

    fun getOnChange() =
        onChange

    fun getVisible() =
        visible

    fun toRusherSetting(): RusherSetting<*>? {
        val setting: RusherSetting<*>? = when (value) {
            is Number -> NumberSetting(name, value as Number, min as Number, max as Number)
            is Boolean -> BooleanSetting(name, value as Boolean)
            is Color -> RusherColorSetting(name, value as Color)
            is Enum<*> -> RusherEnumSetting(name, value as Enum<*>)
            is List<*> -> null /* ToDo */
            else -> null
        }

        @Suppress("UNCHECKED_CAST")
        return setting?.apply {
            setDescription(description)
            setVisibility { visible.get() }
            onChange { newValue -> setValue(newValue as T) }
        }
    }

    fun toMeteorSetting(): Setting<*>? {
        @Suppress("UNCHECKED_CAST")
        val builder: SettingBuilder<*, T, Setting<*>>? = when (value) {
            is Int -> IntSetting.Builder()
                .min(min as Int)
                .max(max as Int)
                .sliderMin(sliderMin as Int)
                .sliderMax(sliderMax as Int) as SettingBuilder<*, T, Setting<*>>

            is Float -> DoubleSetting.Builder()
                .min((min as Float).toDouble())
                .max((max as Float).toDouble())
                .sliderMin((sliderMin as Float).toDouble())
                .sliderMax((sliderMax as Float).toDouble())
                .decimalPlaces(decimalPlacesFromNum(step)) as SettingBuilder<*, T, Setting<*>>

            is Double -> DoubleSetting.Builder()
                .min(min as Double)
                .max(max as Double)
                .sliderMin(sliderMin as Double)
                .sliderMax(sliderMax as Double)
                .decimalPlaces(decimalPlacesFromNum(step)) as SettingBuilder<*, T, Setting<*>>

            is Boolean -> BoolSetting.Builder() as SettingBuilder<*, T, Setting<*>>
            is Color -> ColorSetting.Builder() as SettingBuilder<*, T, Setting<*>>
            is Enum<*> -> EnumSetting.Builder() as SettingBuilder<*, T, Setting<*>>
            is List<*> -> null /* ToDo */
            else -> null
        }

        return builder?.run {
            name(name)
            description(description)
            defaultValue(value)
            onChanged { newValue -> setValue(newValue) }
            visible { visible.get() }
            build()
        }
    }

    private fun decimalPlacesFromNum(number: T): Int {
        val toString = number.toString()
        if (toString.contains(".")) {
            return toString.replaceBefore(".", "").replace(".", "").length
        }
        return 0
    }
}