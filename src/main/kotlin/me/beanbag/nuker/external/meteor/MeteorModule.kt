package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.module.Module
import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.minecraft.util.Formatting
import java.util.function.Consumer
import meteordevelopment.meteorclient.systems.modules.Module as MeteorModule

class MeteorModule(var module: Module) : MeteorModule(MeteorLoader.CATEGORY, module.name, module.description) {

    init {
        for (settingGroup in module.settingGroups) {
            val group = settings.createGroup(settingGroup.name)
            for (setting in settingGroup.settings) {
                group.add(setting.getMeteorSetting())
            }
        }
        module.enabledSetting.getOnChange().add(Consumer{ value -> if(this.isActive != value) this.toggle()})
        val bind = module.keybind
        keybind.set(bind.isKey, bind.value, bind.modifiers)
    }

    override fun toggle() {
        super.toggle()
        module.enabledSetting.setValue(isActive)
        ChatUtils.sendMsg(
            this.hashCode(),
            Formatting.GRAY,
            "Toggled (highlight)%s(default) %s(default).",
            title,
            if (isActive) Formatting.GREEN.toString() + "on" else Formatting.RED.toString() + "off"
        )
    }
}