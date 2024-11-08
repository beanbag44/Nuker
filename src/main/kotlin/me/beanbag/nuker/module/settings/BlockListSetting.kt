package me.beanbag.nuker.module.settings

import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.rusherhack.core.setting.NullSetting
import java.util.function.Consumer
import meteordevelopment.meteorclient.settings.Setting as MeteorSetting
import org.rusherhack.core.setting.Setting as RusherSetting

class BlockListSetting(
    name: String,
    description: String,
    defaultValue: List<Block>,
    onChanged: MutableList<Consumer<List<Block>>>?,
    visible: () -> Boolean,
    filter: (Block) -> Boolean
) : AbstractListSetting<Block>(name, description, defaultValue, onChanged, visible, filter) {
    override fun listValueFromString(value: String): Block? = Registries.BLOCK.getOrEmpty(Identifier(value))?.get()

    override fun listValueToString(value: Block): String =
        Registries.BLOCK.getId(value).toString().replace("minecraft:", "")

    override fun allPossibleValues(): List<String> =
        Registries.BLOCK.ids.map { it.toString().replace("minecraft:", "") }

    override fun toRusherSetting(): RusherSetting<*> {
        val rhSetting = NullSetting(getName(), getDescription())

        rhSetting.setVisibility { isVisible() }

        return rhSetting
    }

    override fun toMeteorSetting(): MeteorSetting<*> {
        val builder = meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value: List<Block> -> setValue(value) }
            .visible { isVisible() }

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value)})

        return meteorSetting
    }
}