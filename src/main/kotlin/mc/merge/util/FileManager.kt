package mc.merge.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import mc.merge.ModCore
import mc.merge.ModCore.meteorIsLoaded
import mc.merge.ModCore.meteorIsPresent
import mc.merge.event.events.GameQuitEvent
import mc.merge.event.onEvent
import mc.merge.ModCore.modules
import meteordevelopment.meteorclient.systems.Systems
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Path
import java.util.Date

object FileManager {
    private var isLoadingSettings = false
    private var saving = false
    private var awaitingSecondSave = false
    private var lastConfigSave: Date? = null
    private const val ONE_SECOND = 1000

    init{
        onEvent<GameQuitEvent> {
            saveModuleConfigs()
        }
    }

    fun getMinecraftDir(): Path {
        return FabricLoader.getInstance().gameDir
    }

    fun getConfigDir(): Path {
        return getMinecraftDir().resolve("config")
    }

    fun configFile(): File {
        return getConfigDir().resolve("${ModCore.MOD_NAME}.json").toFile()
    }

    fun saveModuleConfigs() {
        if ((meteorIsPresent && !meteorIsLoaded) || isLoadingSettings || (saving && awaitingSecondSave)) {
            return
        }

        if (saving) {
            awaitingSecondSave = true
        } else {
            startSaveThread()
        }
    }

    private fun startSaveThread() {
        val saveThread = Thread {
            saving = true
            lastConfigSave = Date()

            val modulesObject = JsonObject()
            for (module in modules) {
                modulesObject.add(module.name, module.toJson())
            }
            getConfigDir().toFile().mkdirs()
            configFile().createNewFile() // creates if it doesn't already exist

            configFile().writeText(
                GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonObject().apply { add("modules", modulesObject) })
            )
            if (meteorIsPresent) {
                Systems.save()
            }

            if (awaitingSecondSave) {
                awaitingSecondSave = false
                startSaveThread()
            } else {
                saving = false
            }
        }

        saveThread.start()
    }

    fun loadModuleConfigs() {
        val configFile = configFile()
        if (!configFile.exists()) return
        isLoadingSettings = true

        val rootObject = Gson().fromJson(configFile.readText(), JsonObject::class.java)
        val modulesObject = rootObject.getAsJsonObject("modules")
        for (module in modules) {
            val moduleObject = modulesObject.getAsJsonObject(module.name)
            if (moduleObject != null) {
                module.fromJson(moduleObject)
            }
        }
        isLoadingSettings = false
    }
}