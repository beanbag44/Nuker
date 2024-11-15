package me.beanbag.nuker.module.settings

import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.rusherhack.core.setting.NullSetting
import org.rusherhack.core.setting.Setting
import java.util.function.Consumer

class EntityTypeListSetting(
    name: String,
    description: String,
    defaultValue: List<EntityType<*>>,
    onChanged: MutableList<Consumer<List<EntityType<*>>>>?,
    visible: () -> Boolean,
    filter: (EntityType<*>) -> Boolean
    ):AbstractListSetting<EntityType<*>>(name, description, defaultValue, onChanged, visible, filter) {
    @Suppress("RedundantNullableReturnType")
    override fun listValueFromString(value: String): EntityType<*>? = Registries.ENTITY_TYPE.get(Identifier(value))

    override fun listValueToString(value: EntityType<*>): String = Registries.ENTITY_TYPE.getId(value).toString().replace("minecraft:", "")

    override fun allPossibleValues(): List<String> =  Registries.ENTITY_TYPE.ids.map { it.toString().replace("minecraft:", "") }

    override fun toRusherSetting(): Setting<*> {
        val rhSetting = NullSetting(getName(), getDescription())

        rhSetting.setVisibility { isVisible() }

        return rhSetting
    }

    override fun toMeteorSetting(): meteordevelopment.meteorclient.settings.Setting<*> {
        val builder = meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue().toSet())
            .onChanged { value: Set<EntityType<*>> -> setValue(value.toList()) }
            .visible { isVisible() }
            .filter(filter)

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value.toSet())})

        return meteorSetting
    }
}