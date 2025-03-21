package mc.merge.module.settings

import mc.merge.util.Versioned
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import java.util.function.Consumer
import java.util.function.Supplier

class BlockSetting(
    name: String,
    description: String,
    defaultValue: Block,
    onChanged: MutableList<Consumer<Block>>?,
    visible: Supplier<Boolean>,
    val filter: (Block) -> Boolean
) : AbstractSetting<Block>(name, description, defaultValue, onChanged, visible) {

    override fun valueFromString(value: String): Block = Registries.BLOCK.get(Versioned.identifier(value))

    override fun valueToString(): String = Registries.BLOCK.getId(getValue()).toString().apply { replace("minecraft:", "", true) }

    override fun possibleValues(): List<String> = Registries.BLOCK.filter { filter(it) }.map{ it.toString().replace("minecraft:", "") }

}
