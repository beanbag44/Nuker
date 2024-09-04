package me.beanbag.nuker.module.settings

import org.rusherhack.core.setting.EnumSetting
import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class EnumSetting<T : Enum<T>>(
    name: String,
    description: String,
    defaultValue: T,
    onChanged: MutableList<Consumer<T>>?,
    visible: Supplier<Boolean>
) : AbstractSetting<T>(name, description, defaultValue, onChanged, visible) {

    private fun possibleEnumValues(): List<T> {
        return getValue().declaringJavaClass.enumConstants.toList()
    }

    override fun possibleValues(): List<String> {
        return possibleEnumValues().map { it.name }
    }

    override fun valueFromString(value: String): T? {
        return possibleEnumValues().firstOrNull { it.name.equals(value, true) }
    }

    override fun valueToString(): String = getValue().name


    fun getDescriptionWithEnum(): String {
        return "${getDescription()} _ Possible values: _ ${(possibleEnumValues().joinToString(separator = " _ ") { it.name + if (it is Describable) ": " + (it as Describable).description else "" })}"
    }

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = EnumSetting(getName(), getDescriptionWithEnum(), getValue())

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange { value -> setValue(value!!) }
        getOnChange().add(Consumer { value -> rhSetting.value = value })

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.EnumSetting.Builder<T>()
            .name(getName())
            .description(getDescriptionWithEnum())
            .defaultValue(getValue())
            .onChanged { value: T -> setValue(value) }
            .visible { isVisible() }
        val meteorSetting = builder.build()
        getOnChange().add(Consumer { value -> meteorSetting.set(value) })
        return meteorSetting
    }
}

interface Describable {
    val description: String
}