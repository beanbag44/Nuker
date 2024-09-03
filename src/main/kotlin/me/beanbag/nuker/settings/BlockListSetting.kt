package me.beanbag.nuker.settings

import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class BlockListSetting(
    name: String,
    description: String,
    defaultValue: List<Block>,
    onChanged: MutableList<Consumer<List<Block>>>?,
    visible: () -> Boolean
) : AbstractSetting<List<Block>>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): List<Block>? {
        val optionalBlocks = value.split(",")
            .map { Registries.BLOCK.getOrEmpty(Identifier(it)) }

        if (optionalBlocks.any { it.isEmpty }) return null

        val blocks = optionalBlocks.map { it.get() }

        return blocks
    }

    override fun valueToString(): String {
        return getValue().joinToString(separator = ",") { Registries.BLOCK.getId(it).toString().replace("minecraft:", "") }
    }

    override fun possibleValues(): List<String> =
        Registries.BLOCK.ids.map { it.toString().replace("minecraft:", "") }


    override fun toRusherSetting(): RusherSetting<*>? {
        TODO("Not yet implemented")
    }

    override fun toMeteorSetting(): MeteorSetting<*>? {
        TODO("Not yet implemented")
    }
}