package me.beanbag.nuker.settings

import org.rusherhack.core.setting.EnumSetting
import java.util.function.Consumer
import java.util.function.Supplier
import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

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
        return possibleEnumValues().firstOrNull{ it.name.equals(value, true) }
    }

    override fun valueToString(): String = getValue().name


    override fun toRusherSetting(): RusherSetting<*> {
        //TODO("Setup onChange")
        val setting = EnumSetting(getName(), getDescription(), getValue())
        setting.setVisibility { isVisible() }
        return setting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        //TODO("Setup visible and onChange")
        val builder = meteordevelopment.meteorclient.settings.EnumSetting.Builder()
            .name(getName())
            .defaultValue(getValue())
            .onChanged { value: Enum<*>? -> setValue(value as T) }
        return builder.build()
    }


}