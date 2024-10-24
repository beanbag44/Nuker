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
    defaultValue: Set<EntityType<*>>,
    onChanged: MutableList<Consumer<Set<EntityType<*>>>>?,
    visible: () -> Boolean,
    val filter: (EntityType<*>) -> Boolean
    ):AbstractSetting<Set<EntityType<*>>>(name, description, defaultValue, onChanged, visible) {
    override fun valueFromString(value: String): Set<EntityType<*>>? {
        val optionalEntityTypes = value.split(",")
            .map { Registries.ENTITY_TYPE.getOrEmpty(Identifier(it)) }

        if (optionalEntityTypes.any { it.isEmpty }) return null

        val entityTypes = optionalEntityTypes.map { it.get() }

        return entityTypes.toSet()
    }

    override fun valueToString(): String {
        return getValue().joinToString(separator = ",") { Registries.ENTITY_TYPE.getId(it).toString().replace("minecraft:", "") }
    }

    override fun possibleValues(): List<String> =
        Registries.ENTITY_TYPE.ids.map { it.toString().replace("minecraft:", "") }.filter { filter(Registries.ENTITY_TYPE.get(Identifier(it))) }


    override fun toRusherSetting(): Setting<*> {
        val rhSetting = NullSetting(getName(), getDescription())

        rhSetting.setVisibility { isVisible() }

        return rhSetting
    }

    override fun toMeteorSetting(): meteordevelopment.meteorclient.settings.Setting<*> {
        val builder = meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
            .name(getName())
            .description(getDescription())
            .defaultValue(getDefaultValue())
            .onChanged { value: Set<EntityType<*>> -> setValue(value) }
            .visible { isVisible() }

        val meteorSetting = builder.build()
        getOnChange().add(Consumer{value -> meteorSetting.set(value)})

        return meteorSetting
    }
}