package mc.merge.external.meteor

import mc.merge.ModCore
import mc.merge.ModCore.getModuleByClass
import mc.merge.module.Module
import mc.merge.module.modules.*
import mc.merge.module.modules.nuker.Nuker
import java.util.function.Consumer
import kotlin.reflect.KClass


abstract class MeteorModule(var module: Module) : meteordevelopment.meteorclient.systems.modules.Module(MeteorLoader.CATEGORY, module.name, module.description) {
    companion object {
        val meteorModules :MutableList<MeteorModule> = mutableListOf()

        private fun MutableList<MeteorModule>.addIfPresent(moduleKlass: KClass<out Module>) {
            val module = getModuleByClass(moduleKlass.java)
            if (module != null) {
                add(generateModule(module))
            }
        }

        private fun generateModule(module: Module): MeteorModule {
            return when (module) {
                is Nuker -> NukerMeteorImplementation(module)
                is CoreConfig -> CoreConfigMeteorImplementation(module)
                is EquipmentSaver -> EquipmentSaverMeteorImplementation(module)
                is FastBreak -> FastBreakMeteorImplementation(module)
                is SafeWalk -> SafeWalkMeteorImplementation(module)
                is SourceRemover -> SourceRemoverMeteorImplementation(module)

                else -> throw IllegalArgumentException("Unknown module type: ${module::class.java}")
            }
        }
        init {
            ModCore.modules.forEach { module ->
                meteorModules.addIfPresent(module::class)
            }
        }
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

//nuker modules
class NukerMeteorImplementation(module: Module) : MeteorModule(module)
class CoreConfigMeteorImplementation(module: Module) : MeteorModule(module)
class EquipmentSaverMeteorImplementation(module: Module) : MeteorModule(module)
class FastBreakMeteorImplementation(module: Module) : MeteorModule(module)
class SafeWalkMeteorImplementation(module:Module) : MeteorModule(module)
class SourceRemoverMeteorImplementation(module: Module) : MeteorModule(module)
