package me.beanbag.nuker.module.settings

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.rusherhack.core.setting.NullSetting
import org.rusherhack.core.setting.Setting
import java.util.function.Consumer

class ItemListSetting(name: String,
                      description: String,
                      defaultValue: List<Item>,
                      onChanged: MutableList<Consumer<List<Item>>>?,
                      visible: () -> Boolean,
                      val filter: (Item) -> Boolean
):AbstractSetting<List<Item>>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): List<Item>? {
        val optionalItems = value.split(",")
            .map { Registries.ITEM.getOrEmpty(Identifier(it)) }

        if (optionalItems.any { it.isEmpty }) return null

        val itemTypes = optionalItems.map { it.get() }

        return itemTypes
    }

    override fun valueToString(): String {
        return getValue().joinToString(separator = ",") {
            Registries.ITEM.getId(it).toString().replace("minecraft:", "")
        }
    }

    override fun possibleValues(): List<String> =
        Registries.ITEM.ids.map { it.toString().replace("minecraft:", "") }.filter {
            filter(
                Registries.ITEM.get(
                    Identifier(it)
                )
            )
        }


    override fun toRusherSetting(): Setting<*> {
        val rhSetting = NullSetting(getName(), getDescription())

        rhSetting.setVisibility { isVisible() }

        return rhSetting
    }

    override fun toMeteorSetting(): meteordevelopment.meteorclient.settings.Setting<*> {
        val builder = meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value -> setValue(value) }
            .visible { isVisible() }
            .filter(filter)

        val meteorSetting = builder.build()
        getOnChange().add(Consumer { value -> meteorSetting.set(value) })

        return meteorSetting
    }
}