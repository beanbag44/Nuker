package me.beanbag.plugins;

import com.mojang.logging.LogUtils;
import me.beanbag.Nuker;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class RusherPluginLoader extends Plugin {
    public void onLoad() {
        final RusherhackModule nukerPlugin = new RusherhackModule();
        RusherHackAPI.getModuleManager().registerFeature(nukerPlugin);
        Nuker.rusherhackLoaded = true;
        LogUtils.getLogger().info("Nuker Plugin Loaded");
    }
    public void onUnload() {
        LogUtils.getLogger().info("Nuker Plugin Unloaded!");
    }
}