package mc.merge.module.settings

import kotlinx.serialization.Serializable

class NukerPreset<T: Any> (
    var selected: String?,
    var entries: MutableMap<String?, AbstractListSetting<T>>
) {
    fun getEntry(key: String?): AbstractListSetting<T>? {
        return entries[key]
    }

    fun addEntry(value: String?, setting: AbstractListSetting<T>) {
        entries[value] = setting
    }
}

@Serializable
data class NukerPresetDTO(
    val selected: String,
    val entries: Map<String, List<String>>
)

