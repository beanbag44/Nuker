package mc.merge.module.settings

import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer

class BlockListSetting(
    name: String,
    description: String,
    defaultValue: List<Block>,
    onChanged: MutableList<Consumer<List<Block>>>?,
    visible: () -> Boolean,
    filter: (Block) -> Boolean
) : AbstractListSetting<Block>(name, description, defaultValue, onChanged, visible, filter) {

    @Suppress("RedundantNullableReturnType")
    override fun listValueFromString(value: String): Block? =
        Registries.BLOCK.get(Identifier(value))

    override fun listValueToString(value: Block): String =
        Registries.BLOCK.getId(value).toString().replace("minecraft:", "")

    override fun allPossibleValues(): List<String> =
        Registries.BLOCK.ids.map { it.toString().replace("minecraft:", "") }
}