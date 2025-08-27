package mc.merge.module.settings

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mc.merge.util.Versioned
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.registry.Registries
import java.util.function.Consumer

class BlockPresetSetting(
    name: String,
    description: String,
    defaultValue: NukerPreset<Block>,
    onChanged: MutableList<Consumer<NukerPreset<Block>>>?,
    visible: () -> Boolean,
    filter: (Block) -> Boolean,
) : AbstractPresetSetting<Block>(name, description, defaultValue, onChanged, visible, filter) {

    override fun valueFromString(value: String): NukerPreset<Block> {
        val dto = Json.decodeFromString<NukerPresetDTO>(value)
        return dto.toBlockPreset()
    }

    override fun valueToString(): String {
        val dto = getValue().toDTO()
        return Json.encodeToString(dto)
    }

    override fun possibleValues(): List<String> {
        return Registries.BLOCK.ids.map { id -> id.toString() }.filter { filter(Registries.BLOCK.get(Versioned.identifier(it))) }
    }

    fun NukerPreset<Block>.toDTO(): NukerPresetDTO {
        val map = mutableMapOf<String, List<String>>()

        entries.forEach { (key, setting) ->
            if (setting is BlockListSetting) {
                map[key ?: ""] = setting.getValue()
                    .map { setting.listValueToString(it) }
            }
        }

        return NukerPresetDTO(selected ?: "", map)
    }

    fun NukerPresetDTO.toBlockPreset(): NukerPreset<Block> {
        val preset = NukerPreset<Block>(selected, mutableMapOf())

        entries.forEach { (key, blockNames) ->
            val setting = BlockListSetting(
                name = key,
                description = "Preset generated for $key",
                defaultValue = blockNames.map { Registries.BLOCK.get(Versioned.identifier(it)) },
                onChanged = null,
                visible = { true },
                filter = { block -> block.hardness >= 0 }
            )
            preset.addEntry(key, setting)
        }

        return preset
    }

    companion object {
        fun defaultPreset(): NukerPreset<Block> {
            return NukerPreset(
                "Underground",
                mutableMapOf(
                    "Underground" to BlockListSetting(
                        name = "Underground",
                        description = "Underground preset list",
                        defaultValue = listOf(
                            Blocks.STONE,
                            Blocks.COBBLESTONE,
                            Blocks.GRANITE,
                            Blocks.DIORITE,
                            Blocks.ANDESITE,
                            Blocks.GRAVEL,
                            Blocks.DIRT
                        ),
                        onChanged = mutableListOf(),
                        visible = { true },
                        filter = { block -> block.hardness >= 0 }
                    ))
            )
        }
    }
}