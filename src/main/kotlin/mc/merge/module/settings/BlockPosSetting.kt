package mc.merge.module.settings

import net.minecraft.util.math.BlockPos
import java.util.function.Consumer

class BlockPosSetting(
    name: String,
    description: String,
    defaultValue: BlockPos,
    onChanged: MutableList<Consumer<BlockPos>>?,
    visible: () -> Boolean
): AbstractSetting<BlockPos>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): BlockPos? {
        val split = value.dropLast(1).drop(1).split(", ")
        if (split.size != 3) return null
        return BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())
    }

    override fun valueToString(): String {
        return "(${getValue().x}, ${getValue().y}, ${getValue().z})"
    }

    override fun possibleValues(): List<String>? {
        return null
    }
}