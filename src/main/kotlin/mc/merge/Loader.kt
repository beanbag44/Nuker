package mc.merge

import mc.merge.ModCore.LOGGER
import mc.merge.ModCore.meteorIsLoaded
import mc.merge.ModCore.meteorIsPresent
import mc.merge.ModCore.modName
import mc.merge.event.EventBus
import mc.merge.util.FileManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.Bootstrap
import java.lang.reflect.Field
import java.lang.reflect.Proxy

class Loader: ModInitializer {
    override fun onInitialize() {
        meteorIsPresent = FabricLoader.getInstance().getModContainer("meteor-client").isPresent

        LOGGER.info("Initialized $modName")
        tryInitialize()
    }

    companion object {
        var rusherPluginAlreadyExists = false
        fun tryInitialize() {
            if (meteorIsPresent && !meteorIsLoaded) {
                return
            }
//            GUI.initGUI()
            FileManager.loadModuleConfigs()
            val rusherPluginJVMArg = System.getProperty("rusherhack.enablePlugins")
            if (FabricLoader.getInstance().isModLoaded("rusherhack")
                && rusherPluginJVMArg != null
                && rusherPluginJVMArg == "true"
            ) {
                FileManager.addRusherPlugin()
                if (!rusherPluginAlreadyExists) {
                    reloadRusherPlugins()
                }
            }

            for (module in ModCore.modules) {
                if (!module.enabled) {
                    EventBus.unsubscribe(module)
                }
            }
        }

        /** Makes this call via reflection:
         *
         * `RusherHackAPI.getCommandManager().dispatcher.execute(commandSource, "reload")`
         *
         * Where `commandSource` is an implementation of `ICommandSource`
         */
        private fun reloadRusherPlugins() {

            try {
                val knotClassloader: ClassLoader = Bootstrap::class.java.getClassLoader()

                val rusherClassloader: Field = knotClassloader.javaClass.getDeclaredField("originalLoader")
                rusherClassloader.setAccessible(true)
                val classLoader = rusherClassloader.get(knotClassloader) as ClassLoader


                val rusherHackAPIClass = classLoader.loadClass("org.rusherhack.client.api.RusherHackAPI")

                val getCommandManagerMethod = rusherHackAPIClass.getMethod("getCommandManager")
                val commandManager = getCommandManagerMethod.invoke(null)
                val commandManagerClass = commandManager::class.java
                val getDispatcherMethod = commandManagerClass.getMethod("getDispatcher")
                val dispatcher = getDispatcherMethod.invoke(commandManager)
                val dispatcherClass = dispatcher::class.java

                val iCommandClass = classLoader.loadClass("org.rusherhack.core.command.ICommandSource")
                val proxyCommandSource = Proxy.newProxyInstance(
                    classLoader,
                    arrayOf(iCommandClass)
                ) { _, _, _ -> true }

                val executeMethod = dispatcherClass.getMethod("execute", iCommandClass, String::class.java)
                executeMethod.invoke(dispatcher, proxyCommandSource, "reload")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}