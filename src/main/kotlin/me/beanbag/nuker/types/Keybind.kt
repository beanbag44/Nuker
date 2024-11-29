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

    fun matches(value: Int, modifiers: Int, isKey: Boolean): Boolean {
        if (value == -1 || isKey != this.isKey || value != this.value) return false
        return if (isKey && modifiers != 0)
            modifiers == this.modifiers
        else
            true
    }

    companion object {
        fun fromString(value: String): Keybind {
            val values = value.split(" ")
            return Keybind(values[0].toInt(), values[1].toInt(), values[2].toBoolean())
        }
    }
}