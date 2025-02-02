package mc.merge.module.settings
import java.util.function.Consumer

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

}
