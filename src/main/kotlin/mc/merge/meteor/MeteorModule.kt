package mc.merge.meteor

import mc.merge.ModCore
import mc.merge.module.Module
import mc.merge.module.modules.nuker.Nuker
import mc.merge.module.modules.*
import me.beanbag.nuker.module.modules.SourceRemover
import java.util.function.Consumer


abstract class MeteorModule(var module: Module) : meteordevelopment.meteorclient.systems.modules.Module(
    MeteorLoader.CATEGORY,
    module.name.replace(" ", ""),
    module.description
) {
    companion object {
        val modules = listOf(
            NukerMeteorImplementation(ModCore.getModuleByClass(Nuker::class.java)!!),
            CoreConfigMeteorImplementation(ModCore.getModuleByClass(CoreConfig::class.java)!!),
            EquipmentSaverMeteorImplementation(ModCore.getModuleByClass(EquipmentSaver::class.java)!!),
            FastBreakMeteorImplementation(ModCore.getModuleByClass(FastBreak::class.java)!!),
            SafeWalkMeteorImplementation(ModCore.getModuleByClass(SafeWalk::class.java)!!),
            SourceRemoverMeteorImplementation(ModCore.getModuleByClass(SourceRemover::class.java)!!),
            UnfocusedCPUMeteorImplementation(ModCore.getModuleByClass(UnfocusedCPU::class.java)!!),
        )
    }
    init {
        val settingBuilder = MeteorSettingBuilder()

        for (settingGroup in module.settingGroups) {
            val group = settings.createGroup(settingGroup.name)
            for (setting in settingGroup.settings) {
                group.add(settingBuilder.toMeteorSetting(setting))
            }
        }
        module.enabledSetting.getOnChange().add(Consumer{ value -> if(this.isActive != value) this.toggle()})
    }

    override fun toggle() {
        super.toggle()
        module.enabledSetting.setValue(isActive)
    }
}


class NukerMeteorImplementation(module: Module) : MeteorModule(module)

class CoreConfigMeteorImplementation(module: Module) : MeteorModule(module)

class EquipmentSaverMeteorImplementation(module: Module) : MeteorModule(module)

class FastBreakMeteorImplementation(module: Module) : MeteorModule(module)

class SafeWalkMeteorImplementation(module: Module) : MeteorModule(module)

class SourceRemoverMeteorImplementation(module: Module) : MeteorModule(module)

class UnfocusedCPUMeteorImplementation(module: Module) : MeteorModule(module)
