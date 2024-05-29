package me.beanbag.plugins;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class MeteorAddonLoader extends MeteorAddon {

    public static final Category CATEGORY = new Category("Nuker");
    @Override
    public void onInitialize() {
        LogUtils.getLogger().info("Initializing Nuker Addon");
        Modules.get().add(new MeteorModule());
    }
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
    @Override
    public String getPackage() {
        return "me.beanbag.plugins";
    }
    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Beanbag44", "Nuker");
    }
}
