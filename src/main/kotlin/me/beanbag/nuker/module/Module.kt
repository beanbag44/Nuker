package me.beanbag.nuker.module

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.beanbag.nuker.ModConfigs.modColor
import me.beanbag.nuker.command.ExecutableClickEvent
import me.beanbag.nuker.command.commands.HelpModuleSettingCommand
import me.beanbag.nuker.eventsystem.CallbackHolder
import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.Event
import me.beanbag.nuker.handlers.ChatHandler
import me.beanbag.nuker.module.settings.*
import me.beanbag.nuker.utils.IJsonable
import net.minecraft.block.Block
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.awt.Color
import java.util.function.Consumer

abstract class Module(var name: String, var description: String) : IJsonable {
    var settingGroups: MutableList<SettingGroup> = ArrayList()
    val enabledGroup = SettingGroup("Enabled", "Settings for enabling or disabling the module")
    var enabled by setting(enabledGroup,"Enabled", "Enables or disables the module", false, null, visible = { true })

    val callbackHolder = CallbackHolder()

    inline fun <reified T: Event> addListener(noinline callback: (Event) -> Unit) {
        callbackHolder.addCallback<T>(callback)
    }

    private fun unsubscribe() {
        callbackHolder.unsubscribe()
    }

    private fun subscribe() {
        callbackHolder.subscribe()
    }

    init {
        EventBus.addCallbackHolder(callbackHolder)
    }

    protected fun addGroup(setting: SettingGroup): SettingGroup {
        settingGroups.add(setting)
        return setting
    }

    open fun onTick() {
    }

    fun helpText(): Text {
        val text = Text.literal("${ChatHandler.toCamelCaseName(name)} - ${description}\n")

        for (settingGroup in settingGroups) {
            text.append(Text.literal(settingGroup.name).styled { it.withColor(modColor) })
                .append(Text.literal(" - ${settingGroup.description}\n").styled {
                    it.withColor(
                        Formatting.GRAY
                    )
                })
            for (setting in settingGroup.settings) {
                text.append(Text.literal(" " + ChatHandler.toCamelCaseName(setting.getName())).styled {
                    it.withUnderline(true)
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, setting.helpText()))
                        .withClickEvent(ExecutableClickEvent { HelpModuleSettingCommand().execute(listOf("help", ChatHandler.toCamelCaseName(name), ChatHandler.toCamelCaseName(setting.getName()))) })
                })
                    .append(Text.literal(" - ${setting.getDescription()}\n").styled {
                        it.withColor(
                            Formatting.GRAY
                        )
                    })
            }
        }
        return text
    }

    fun enabledText(): Text {
        return Text.literal(if (enabled) "Enabled" else "Disabled")
            .styled { it.withColor(if (enabled) Formatting.GREEN else Formatting.RED) }
    }

    fun group(name: String, description: String) = addGroup(SettingGroup(name, description))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: List<Block>,
        onChange: MutableList<Consumer<List<Block>>>? = null,
        visible: () -> Boolean = { true }
    ) = group.add(BlockListSetting(name, description, defaultValue, onChange, visible))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: Boolean,
        onChange: MutableList<Consumer<Boolean>>? = null,
        visible: () -> Boolean = { true }
    ) = group.add(BoolSetting(name, description, defaultValue, onChange, visible))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: Color,
        onChange: MutableList<Consumer<Color>>? = null,
        visible: () -> Boolean = { true }
    ) = group.add(ColorSetting(name, description, defaultValue, onChange, visible))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: Double,
        onChange: MutableList<Consumer<Double>>? = null,
        visible: () -> Boolean = { true },
        min: Double? = null,
        max: Double? = null,
        sliderMin: Double? = null,
        sliderMax: Double? = null,
        step: Double? = 0.1,
    ) = group.add(DoubleSetting(name, description, defaultValue, onChange, visible, min, max, sliderMin, sliderMax, step))

    inline fun <reified T : Enum<T>> setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: T,
        onChange: MutableList<Consumer<T>>? = null,
        noinline visible: () -> Boolean = { true }
    ) = group.add(EnumSetting(name, description, defaultValue, onChange, visible))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: Float,
        onChange: MutableList<Consumer<Float>>? = null,
        visible: () -> Boolean = { true },
        min: Float? = null,
        max: Float? = null,
        sliderMin: Float? = null,
        sliderMax: Float? = null,
        step: Float? = null,
    ) = group.add(FloatSetting(name, description, defaultValue, onChange, visible, min, max, sliderMin, sliderMax, step))

    fun setting(
        group: SettingGroup,
        name: String,
        description: String,
        defaultValue: Int,
        onChange: MutableList<Consumer<Int>>? = null,
        visible: () -> Boolean = { true },
        min: Int? = null,
        max: Int? = null,
        sliderMin: Int? = null,
        sliderMax: Int? = null,
    ) = group.add(IntSetting(name, description, defaultValue, onChange, visible, min, max, sliderMin, sliderMax))

    // To support Java
    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: List<Block>
    ) = setting(group, name, description, defaultValue, null)

    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: Boolean
    ) = setting(group, name, description, defaultValue, null)

    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: Color
    ) = setting(group, name, description, defaultValue, null)

    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: Double
    ) = setting(group, name, description, defaultValue, null)

    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: Float
    ) = setting(group, name, description, defaultValue, null)

    fun setting(
        group: SettingGroup, name: String, description: String, defaultValue: Int
    ) = setting(group, name, description, defaultValue, null)


    override fun toJson(): JsonElement {
        val settings = settingGroups.flatMap { it.settings }
        val obj = JsonObject()
        for (setting in settings) {
            obj.add(setting.getName(), setting.toJson())
        }
        obj.add("enabled", enabledGroup.settings[0].toJson())
        return obj
    }

    override fun fromJson(json: JsonElement) {
        val settings = settingGroups.flatMap { it.settings }
        for (setting in settings) {
            val settingJson = json.asJsonObject.get(setting.getName())
            if (settingJson != null) {
                setting.fromJson(settingJson)
            }
        }
        val enabledJson = json.asJsonObject.get("enabled")
        if (enabledJson != null) {
            enabledGroup.settings[0].fromJson(enabledJson)
        }
    }
}