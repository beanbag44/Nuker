package me.beanbag.nuker.external.rusher

import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.command.commands.*
import org.rusherhack.client.api.feature.command.Command
import org.rusherhack.core.command.annotations.CommandExecutor

class RusherCommands :Command(ModConfigs.COMMAND_PREFIX, "Nuker commands") {
    @CommandExecutor
    private fun noInput(): String? {
        HelpCommand().execute(listOf())
        return null
    }

    @CommandExecutor(subCommand = ["help"])
    private fun help(): String? {
        HelpCommand().execute(listOf())
        return null
    }

    @CommandExecutor(subCommand = ["helpModules"])
    private fun helpModules(): String? {
        HelpModulesCommand().execute(listOf())
        return null
    }

    @CommandExecutor(subCommand = ["help"])
    @CommandExecutor.Argument("module")
    private fun helpModule(string: String): String? {
        HelpModuleCommand().execute(listOf("", string))
        return null
    }

    @CommandExecutor(subCommand = ["help"])
    @CommandExecutor.Argument("module", "setting")
    private fun helpModuleSetting(string: String, string2: String): String? {
        HelpModuleSettingCommand().execute(listOf("", string, string2))
        return null
    }

    @CommandExecutor(subCommand = ["list"])
    private fun list(): String? {
        ListCommand().execute(listOf(""))
        return null
    }

    @CommandExecutor(subCommand = ["list"])
    @CommandExecutor.Argument("module")
    private fun listModule(string: String): String? {
        ListModuleCommand().execute(listOf("", string))
        return null
    }

    @CommandExecutor
    @CommandExecutor.Argument("module")
    private fun toggleModule(string: String): String? {
        ToggleModuleCommand().execute(listOf("", string))
        return null
    }

    @CommandExecutor
    @CommandExecutor.Argument("module", "setting", "value")
    private fun setModuleSetting(string: String, string2: String, string3: String): String? {
        SetModuleSettingCommand().execute(listOf("", string, string2, string3))
        return null
    }
}