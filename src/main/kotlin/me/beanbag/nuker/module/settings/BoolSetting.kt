package me.beanbag.nuker.module.settings
import org.rusherhack.core.setting.BooleanSetting
import java.util.function.Consumer
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class BoolSetting(
    name: String,
    description: String,
    defaultValue: Boolean,
    onChanged: MutableList<Consumer<Boolean>>?,
    visible: () -> Boolean
) : AbstractSetting<Boolean>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): Boolean? = value.toBooleanStrictOrNull()

    override fun valueToString(): String = getValue().toString()

    override fun possibleValues(): List<String> = listOf("true", "false")

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = BooleanSetting(getName(), getDescription(), getValue())

        rhSetting.setVisibility { isVisible() }
        rhSetting.onChange{value -> setValue(value)}
        getOnChange().add(Consumer{value -> rhSetting.value = value})

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        val builder = meteordevelopment.meteorclient.settings.BoolSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value: Boolean -> setValue(value) }
            .visible { isVisible() }

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value)})

        return meteorSetting
    }
}
