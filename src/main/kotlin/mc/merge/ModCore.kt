package mc.merge

import mc.merge.command.commands.*
import mc.merge.command.ICommand
import mc.merge.handler.InventoryHandler
import mc.merge.module.Module
import mc.merge.module.modules.nuker.Nuker
import mc.merge.module.modules.*
import me.beanbag.nuker.module.modules.*
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object ModCore {
    //Mod specific
    val modColor = Formatting.BLUE.colorValue ?: Color(0, 0, 255).rgb
    const val MOD_NAME = "Nuker"
    const val COMMAND_PREFIX = "&&"
    const val MOD_ID = "nuker"

    val mc: MinecraftClient = MinecraftClient.getInstance()
    var meteorIsPresent = false
    var meteorIsLoaded = false
    var rusherIsPresent = false
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)

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

    var modules = listOf<Module>(
        CoreConfig,
        Nuker(),
        SourceRemover(),
        UnfocusedCPU(),
        FastBreak(),
        EquipmentSaver(),
        SafeWalk(),
    )

    fun getModuleByName(name: String): Module? {
        return modules.find { it.name.equals(name, true) }
    }

    fun <T : Module> getModuleByClass(clazz: Class<out T>): T? {
        @Suppress("UNCHECKED_CAST")
        return modules.find { it.javaClass == clazz } as T?
    }
}