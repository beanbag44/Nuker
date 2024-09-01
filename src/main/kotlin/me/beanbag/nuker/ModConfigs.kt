package me.beanbag.nuker

import me.beanbag.nuker.chat.ICommand
import me.beanbag.nuker.chat.commands.*
import net.minecraft.util.Formatting

object ModConfigs {
    val modColor = Formatting.BLUE.colorValue!!
    const val MOD_NAME = "Nuker"
    const val COMMAND_PREFIX = "&&"

    val commands: List<ICommand> = listOf(
        HelpCommand(),
        HelpModulesCommand(),
        HelpModuleCommand(),
        HelpModuleSettingCommand(),
        ListCommand(),
        ListModuleCommand(),
        ToggleModuleCommand(),
        SetModuleSettingCommand(),
    )
}