package mc.merge.module.settings

import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer

class ItemListSetting(name: String,
                      description: String,
                      defaultValue: List<Item>,
                      onChanged: MutableList<Consumer<List<Item>>>?,
                      visible: () -> Boolean,
                      filter: (Item) -> Boolean
): AbstractListSetting<Item>(name, description, defaultValue, onChanged, visible, filter) {
    @Suppress("RedundantNullableReturnType")
    override fun listValueFromString(value: String): Item? = Registries.ITEM.get(Identifier(value))


    override fun listValueToString(value: Item): String =
        Registries.ITEM.getId(value).toString().apply { replace("minecraft:", "", true) }

    override fun allPossibleValues(): List<String> =
        Registries.ITEM.ids.filter { filter(Registries.ITEM.get(it)) }.map { it.toString().replace("minecraft:", "") }
}