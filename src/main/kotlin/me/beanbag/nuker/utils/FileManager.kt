package me.beanbag.nuker.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import me.beanbag.nuker.ModConfigs
import me.beanbag.nuker.ModConfigs.modules
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Path

object FileManager {
    fun getMinecraftDir(): Path {
        return FabricLoader.getInstance().gameDir
    }

    fun getConfigDir(): Path {
        return getMinecraftDir().resolve("config")
    }

    fun configFile(): File {
        return getConfigDir().resolve("${ModConfigs.MOD_NAME.lowercase()}.json").toFile()
    }

    fun saveModuleConfigs() {
        val modulesObject = JsonObject()
        for (module in modules.values) {
            modulesObject.add(module.name, module.toJson())
        }
        getConfigDir().toFile().mkdirs()
        configFile().createNewFile() // creates if it doesn't already exist

        configFile().writeText(
            GsonBuilder().setPrettyPrinting().create()
                .toJson(JsonObject().apply { add("modules", modulesObject) })
        )
    }

    fun loadModuleConfigs() {
        val configFile = configFile()
        if (!configFile.exists()) return

        val rootObject = Gson().fromJson(configFile.readText(), JsonObject::class.java)
        val modulesObject = rootObject.getAsJsonObject("modules")
        for (module in modules.values) {
            val moduleObject = modulesObject.getAsJsonObject(module.name)
            if (moduleObject != null) {
                module.fromJson(moduleObject)
            }
        }

    }
}