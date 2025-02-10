package mc.merge

import mc.merge.command.ICommand
import mc.merge.command.commands.*
import mc.merge.event.events.GameJoinedEvent
import mc.merge.event.onInGameEvent
import mc.merge.handler.ChatHandler
import mc.merge.handler.InventoryHandler
import mc.merge.module.Module
import mc.merge.module.modules.CoreConfig
import mc.merge.module.modules.EquipmentSaver
import mc.merge.module.modules.FastBreak
import mc.merge.module.modules.SafeWalk
import mc.merge.module.modules.nuker.Nuker
import mc.merge.util.FileManager
import me.beanbag.nuker.module.modules.SourceRemover
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.*

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

        onInGameEvent<GameJoinedEvent> {
            val username = mc.session.username.lowercase()

            if (username == "cRxPtiCz".lowercase()) {
                val messages = listOf(
                    "We rise by lifting others",
                    "Community is currency, build bridges, not walls",
                    "You don't have to knock anyone off their game to win yours. It doesn’t build you up to tear others down.",
                    "Always show kindness and love to others. Your words might be filling the empty places in someone's heart.",
                    "If you're not reaching back to help anyone then you're not building a legacy.",
                    "We will be a mighty nation, if we build each other.",
                    "Lets root for each other and watch each other grow",
                    "Leaders instill in their people a hope for success and a belief in themselves. Positive leaders empower people to accomplish their goals.",
                    "No act of kindness, no matter how small, is ever wasted",
                    "Be kind, for everyone you meet is fighting a harder battle",
                    "Shall we make a new rule of life from tonight: always try to be a little kinder than is necessary?",
                    "It's nice to be important, but it's more important to be nice.",
                    "Kindness is free; sprinkle that stuff everywhere.",
                    "Kindness is a gift everyone can afford to give.",
                    "Leave footprints of kindness wherever you go.",
                    "Kind people are my kind of people.",
                    "Kindness is free to give, but priceless to receive",
                    "Sometimes it takes only one act of kindness and caring to change a person's life",
                    "Love and kindness are never wasted. They always make a difference.",
                    "We can't help everyone, but everyone can help someone.",
                    "\"Politeness is the flower of humanity\" - Joseph Joubert",
                )
                ChatHandler.sendChatLine("Welcome cRxPtiCz")
                ChatHandler.sendChatLine(messages.random())
            }
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