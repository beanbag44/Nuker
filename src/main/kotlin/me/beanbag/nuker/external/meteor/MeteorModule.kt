package me.beanbag.nuker.external.meteor

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.module.Module
import me.beanbag.nuker.module.modules.*
import me.beanbag.nuker.module.modules.nuker.Nuker
import java.util.function.Consumer
import meteordevelopment.meteorclient.systems.modules.Module as MeteorModule

abstract class MeteorModule(var module: Module) : MeteorModule(MeteorLoader.CATEGORY, module.name, module.description) {
    companion object {
        val modules = listOf(
            NukerMeteorImplementation(ModConfigs.getModuleByClass(Nuker::class.java)!!),
            CoreConfigMeteorImplementation(ModConfigs.getModuleByClass(CoreConfig::class.java)!!),
            EquipmentSaverMeteorImplementation(ModConfigs.getModuleByClass(EquipmentSaver::class.java)!!),
            FastBreakMeteorImplementation(ModConfigs.getModuleByClass(FastBreak::class.java)!!),
            SafeWalkMeteorImplementation(ModConfigs.getModuleByClass(SafeWalk::class.java)!!),
            SourceRemoverMeteorImplementation(ModConfigs.getModuleByClass(SourceRemover::class.java)!!),
            UnfocusedCPUMeteorImplementation(ModConfigs.getModuleByClass(UnfocusedCPU::class.java)!!),
        )
    }
    init {
        for (settingGroup in module.settingGroups) {
            val group = settings.createGroup(settingGroup.name)
            for (setting in settingGroup.settings) {
                group.add(setting.getMeteorSetting())
            }
        }
        module.enabledSetting.getOnChange().add(Consumer{ value -> if(this.isActive != value) this.toggle()})
    }

    override fun toggle() {
        super.toggle()
        module.enabledSetting.setValue(isActive)
    }
}

class NukerMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class CoreConfigMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class EquipmentSaverMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class FastBreakMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class SafeWalkMeteorImplementation(module:Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class SourceRemoverMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)

class UnfocusedCPUMeteorImplementation(module: Module) : me.beanbag.nuker.external.meteor.MeteorModule(module)
