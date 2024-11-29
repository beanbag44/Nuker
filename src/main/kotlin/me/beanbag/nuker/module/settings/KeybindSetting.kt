package me.beanbag.nuker.module.settings

import me.beanbag.nuker.types.Keybind
import org.rusherhack.core.setting.Setting
import java.util.function.Consumer

class KeybindSetting(
    name: String,
    description: String,
    defaultValue: Keybind,
    onChange: MutableList<Consumer<Keybind>>? = null,
    visible: () -> Boolean = { true }
) : AbstractSetting<Keybind>(name, description, defaultValue, onChange, visible) {
    override fun valueFromString(value: String): Keybind =
        Keybind.fromString(value)

    override fun valueToString(): String =
        getValue().toString()

    override fun possibleValues(): List<String>? {
        return null
    }

    override fun toRusherSetting(): Setting<*>? {
        TODO("Nuh uh")
    }

    override fun toMeteorSetting(): meteordevelopment.meteorclient.settings.Setting<*> {
        TODO("Nuh uh")
    }
}