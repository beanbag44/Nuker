package me.beanbag.nuker.module.settings

import java.util.function.Consumer
import java.util.function.Supplier

abstract class AbstractListSetting<T : Any>(
    name: String,
    description: String,
    defaultValue: List<T>,
    onChange: MutableList<Consumer<List<T>>>?,
    visible: Supplier<Boolean>,
    val filter: (T) -> Boolean
) : AbstractSetting<List<T>>(
    name,
    description, defaultValue, onChange, visible
) {

    abstract fun listValueFromString(value: String): T?
    override fun valueFromString(value: String): List<T>? {
        val optionalValues = value.split(",")
            .map { listValueFromString(it) }
        return optionalValues.filterNotNull()
    }

    abstract fun listValueToString(value: T): String
    override fun valueToString(): String {
        return getValue().joinToString(separator = ",") { listValueToString(it) }
    }

    abstract fun allPossibleValues(): List<String>?
    override fun possibleValues(): List<String>? =
        allPossibleValues()?.filter{ filter(listValueFromString(it)!!) }


    fun addFromString(value: String) {
        setValue(getValue() + (listValueFromString(value)?: return))
    }
    fun removeFromString(value: String) {
        setValue(getValue() - (listValueFromString(value)?: return))
    }
}