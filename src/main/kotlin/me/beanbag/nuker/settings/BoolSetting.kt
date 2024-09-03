package me.beanbag.nuker.settings
import java.util.function.Consumer
import org.rusherhack.core.setting.Setting as RusherSetting
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

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

    override fun toRusherSetting(): RusherSetting<*>? {
        TODO("Not yet implemented")
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        TODO("Not yet implemented")
    }
}
