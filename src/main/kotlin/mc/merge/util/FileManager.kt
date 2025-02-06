package mc.merge.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import mc.merge.Loader.Companion.rusherPluginAlreadyExists
import mc.merge.ModCore
import mc.merge.ModCore.meteorIsLoaded
import mc.merge.ModCore.meteorIsPresent
import mc.merge.ModCore.modules
import mc.merge.event.events.GameQuitEvent
import mc.merge.event.onEvent
import meteordevelopment.meteorclient.systems.Systems
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

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
        return getConfigDir().resolve("${ModCore.modName}.json").toFile()
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

    fun addRusherPlugin() {
        val mergeDetailsJsonString = javaClass.getResourceAsStream("/merge.json")?.bufferedReader()?.use { it.readText() } ?: return

        val json = Gson().fromJson(mergeDetailsJsonString, JsonObject::class.java)
        val modId = json.get("mod_id").asString
        val rusherVersion = json.get("rusher_wrapper_version").asString
        val minecraftVersion = json.get("minecraft_version").asString
        val rusherFileName = "$modId-rusher-$minecraftVersion-$rusherVersion.jar"

        //Delete old rusher plugin(s)
        val rusherPluginDir = getMinecraftDir().resolve("rusherhack/plugins").toFile()
        if (rusherPluginDir.exists()) {
            for (file in rusherPluginDir.listFiles()!!) {
                if (file.name.contains("$modId-rusher")) {
                    if (file.name != rusherFileName) {
                        file.delete()
                    } else {
                        rusherPluginAlreadyExists = true
                    }
                }
            }
        }

        //Copy new rusher plugin(s)
        if (!rusherPluginAlreadyExists) {
            copyResourceToFile("rusherhack/plugins", rusherFileName)
        }
    }

    fun getJsonResource(fileName:String): JsonObject? {
        val resourceContents = javaClass.getResourceAsStream(fileName)?.bufferedReader()?.use { it.readText() } ?: return null
        return Gson().fromJson(resourceContents, JsonObject::class.java)
    }

    private fun copyResourceToFile(gameSubFolder: String, resourceName: String) {
        val gameDirPath = FabricLoader.getInstance().gameDir.toString()
        var fileStream: InputStream? = null
        try {
            Files.createDirectories(File(gameDirPath).toPath().resolve(gameSubFolder))
            fileStream = javaClass.getResourceAsStream("/$resourceName")
            checkNotNull(fileStream)
            Files.copy(
                fileStream,
                File(gameDirPath + (if (gameSubFolder.isNotEmpty()) "/" else "") + gameSubFolder + "/" + resourceName).toPath()
            )
        } catch (e: FileAlreadyExistsException) {
            //This should happen the majority of the time because the file is already there. This is fine.
        } catch (e: Exception) {
            println("Failed to add file: $resourceName")
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }
}