package mc.merge.module.settings

import mc.merge.util.Versioned
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import java.util.function.Consumer
import java.util.function.Supplier

class ItemSetting(
    name: String,
    description: String,
    defaultValue: Item,
    onChanged: MutableList<Consumer<Item>>?,
    visible: Supplier<Boolean>,
    val filter: (Item) -> Boolean
) : AbstractSetting<Item>(name, description, defaultValue, onChanged, visible) {

    override fun valueFromString(value: String): Item = Registries.ITEM.get(Versioned.identifier(value))

    override fun valueToString(): String = Registries.ITEM.getId(getValue()).toString().apply { replace("minecraft:", "", true) }

    override fun possibleValues(): List<String> = Registries.ITEM.filter { filter(it) }.map{ it.toString().replace("minecraft:", "") }

}
