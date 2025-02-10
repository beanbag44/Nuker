package mc.merge.module.settings

import java.util.function.Consumer
import java.util.function.Supplier
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting

@Suppress("UNCHECKED_CAST")
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

    fun setValue(value: Enum<*>) {
        if (value.javaClass != getValue().javaClass) return
        setValue(value as T)
    }

    fun addOnChange(consumer: Consumer<Any>) {
        getOnChange().add(consumer as Consumer<T>)
    }


    fun toMeteorSetting(): MeteorSetting<*> {
        val builder = meteordevelopment.meteorclient.settings.EnumSetting.Builder<T>()
            .name(getName())
            .description(getDescriptionWithEnum())
            .defaultValue(getDefaultValue())
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