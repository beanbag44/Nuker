package me.beanbag.nuker.module.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.utils.FileManager
import me.beanbag.nuker.utils.IJsonable
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

abstract class AbstractSetting<T : Any>(
    private var name: String,
    private var description: String,
    private val defaultValue: T,
    onChange: MutableList<Consumer<T>>?,
    private var visible: Supplier<Boolean>
) : IJsonable {
    private var value: T = defaultValue
    private var onChange: MutableList<Consumer<T>> = onChange ?: mutableListOf()

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setValue(value)
    }

    fun setValue(value: T) {
        if (this.value != value) {
            this.value = value
            onChange.forEach { it.accept(value) }
            FileManager.saveModuleConfigs()
        }
    }

    fun getName() = name
    fun getDescription() = description
    fun getDefaultValue() = defaultValue
    fun getValue() = value
    fun getOnChange() = onChange
    fun isVisible() = visible.get()

    fun setValueFromString(value: String) {
        valueFromString(value)?.let { setValue(it) }
    }

    abstract fun valueFromString(value: String): T?

    abstract fun valueToString(): String

    abstract fun possibleValues(): List<String>?

    protected abstract fun toRusherSetting(): RusherSetting<*>?

    protected abstract fun toMeteorSetting(): MeteorSetting<*>

    fun getMeteorSetting(): MeteorSetting<*> {
        val meteorSetting = toMeteorSetting()
//        onChange.forEach { it.accept(value) }
        return meteorSetting
    }

    fun getRusherSetting(): RusherSetting<*>? {
        val rusherSetting = toRusherSetting()
//        onChange.forEach { it.accept(value) }
        return rusherSetting
    }

    fun helpText(): Text {
        val text = Text.literal("${ChatHandler.toCamelCaseName(name)} - ${description}\n")
        if (value is Enum<*>) {
            text.append(Text.literal("Possible values:\n"))
            for (enum in (value as Enum<*>).declaringJavaClass.enumConstants) {
                text.append(Text.literal(" ${enum.name}"))
                if (value is Describable) {
                    text.append(Text.literal(" - ${(value as Describable).description}").styled { it.withColor(Formatting.GRAY) })
                }
                text.append(Text.literal("\n"))
            }
        }

        return text
    }

    override fun toJson(): JsonElement {
        return JsonPrimitive(valueToString())
    }

    override fun fromJson(json: JsonElement) {
        valueFromString(json.asString)?.let { setValue(it) }
    }
}