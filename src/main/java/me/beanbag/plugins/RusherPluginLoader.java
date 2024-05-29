package me.beanbag.plugins;

import me.beanbag.Nuker;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class RusherPluginLoader extends Plugin {
    @Override
    public void onLoad() {
        final RusherhackModule nukerPlugin = new RusherhackModule();
        RusherHackAPI.getModuleManager().registerFeature(nukerPlugin);
        Nuker.rusherhackLoaded = true;
        this.getLogger().info("Nuker Plugin Loaded");
    }
    @Override
    public void onUnload() {
        this.getLogger().info("Nuker Plugin Unloaded!");
    }
}
