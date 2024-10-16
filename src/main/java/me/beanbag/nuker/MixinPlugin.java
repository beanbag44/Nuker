package me.beanbag.nuker;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class MixinPlugin implements IMixinConfigPlugin {
    // Conditions for applying mixins
    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            "fi.dy.masa.litematica", () -> FabricLoader.getInstance().isModLoaded("litematica"),
            "meteordevelopment.meteorclient", () -> FabricLoader.getInstance().isModLoaded("meteor-client")
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (Map.Entry<String, Supplier<Boolean>> entry : CONDITIONS.entrySet()) {
            if (targetClassName.startsWith(entry.getKey())) {
                return entry.getValue().get();
            }
        }
        return true;
    }

    // Boilerplate

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}