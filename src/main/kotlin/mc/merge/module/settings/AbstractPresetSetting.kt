package mc.merge.module.settings

import java.util.function.Consumer
import java.util.function.Supplier

abstract class AbstractPresetSetting <T : Any>(
    name: String,
    description: String,
    defaultValue: NukerPreset<T>,
    onChange: MutableList<Consumer<NukerPreset<T>>>?,
    visible: Supplier<Boolean>,
    val filter: (T) -> Boolean,
) : AbstractSetting<NukerPreset<T>>(
    name, description, defaultValue, onChange, visible
)