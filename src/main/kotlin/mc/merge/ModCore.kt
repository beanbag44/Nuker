package mc.merge

import mc.merge.command.commands.*
import mc.merge.command.ICommand
import mc.merge.handler.InventoryHandler
import mc.merge.module.Module
import mc.merge.module.modules.nuker.Nuker
import mc.merge.module.modules.*
import mc.merge.util.FileManager
import me.beanbag.nuker.module.modules.*
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object ModCore {
    //Mod specific
    val modColor = Formatting.BLUE.colorValue ?: Color(0, 0, 255).rgb
    var modName = "Nuker"
    var commandPrefix = "&&"
    var modId = "nuker"

    val mc: MinecraftClient = MinecraftClient.getInstance()
    var meteorIsPresent = false
    var meteorIsLoaded = false
    var rusherIsPresent = false
    val LOGGER: Logger = LoggerFactory.getLogger(modName)

    val inventoryHandler = InventoryHandler()

    val commands: List<ICommand> = listOf(
        HelpCommand(),
        HelpModulesCommand(),
        HelpModuleCommand(),
        HelpModuleSettingCommand(),
        ListCommand(),
        ListModuleCommand(),
        ToggleModuleCommand(),
        SetModuleSettingCommand(),
        SetModuleListSettingCommand(),
    )

    var modules = listOf(
        CoreConfig,
        Nuker(),
        SourceRemover(),
        FastBreak(),
        EquipmentSaver(),
        SafeWalk(),
    )

    init {
        val json = FileManager.getJsonResource("/merge.json")
        if (json != null) {
            modId = json.get("mod_id").asString
            modName = json.get("mod_name").asString
            commandPrefix = json.get("command_prefix").asString
//            ModCore.modVersion = json.get("mod_version").asString
//            ModCore.rusherWrapperVersion = json.get("rusher_wrapper_version").asString
//            ModCore.mcVersion = json.get("minecraftVersion").asString
        }
    }

    fun getModuleByName(name: String): Module? {
        return modules.find { it.name.equals(name, true) }
    }

    fun <T : Module> getModuleByClass(clazz: Class<out T>): T? {
        @Suppress("UNCHECKED_CAST")
        return modules.find { it.javaClass == clazz } as T?
    }
}