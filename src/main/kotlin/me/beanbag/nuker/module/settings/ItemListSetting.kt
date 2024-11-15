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
                      filter: (Item) -> Boolean
):AbstractListSetting<Item>(name, description, defaultValue, onChanged, visible, filter) {
    @Suppress("RedundantNullableReturnType")
    override fun listValueFromString(value: String): Item? = Registries.ITEM.get(Identifier(value))


    override fun listValueToString(value: Item): String =
        Registries.ITEM.getId(value).toString().apply { replace("minecraft:", "", true) }

    override fun allPossibleValues(): List<String> =
        Registries.ITEM.ids.map { it.toString().replace("minecraft:", "") }

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