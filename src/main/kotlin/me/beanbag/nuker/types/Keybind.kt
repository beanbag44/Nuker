package me.beanbag.nuker.types

data class Keybind(var value: Int, var modifiers: Int, var isKey: Boolean) {
    fun set(value: Int, modifiers: Int, isKey: Boolean) {
        this.value = value
        this.modifiers = modifiers
        this.isKey = isKey
    }

    override fun toString(): String {
        return "$value $modifiers $isKey"
    }

    fun matches(value: Int, modifiers: Int, isKey: Boolean): Boolean =
        this.value == value
                && this.modifiers == modifiers
                && this.isKey == isKey

    companion object {
        fun fromString(value: String): Keybind {
            val values = value.split(" ")
            return Keybind(values[0].toInt(), values[1].toInt(), values[2].toBoolean())
        }
    }
}