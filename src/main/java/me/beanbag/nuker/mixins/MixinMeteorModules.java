package me.beanbag.nuker.mixins;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ValueComparableMap;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Modules.class)
public class MixinMeteorModules {
    @Shadow(remap = false) @Final private List<Module> modules;

    @Inject(method = "get(Ljava/lang/String;)Lmeteordevelopment/meteorclient/systems/modules/Module;", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGet(String name, CallbackInfoReturnable<Module> cir) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(name)) cir.setReturnValue(module);
        }
    }

    @Inject(method = "getAll()Ljava/util/Collection;", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetAll(CallbackInfoReturnable<List<Module>> cir) {
        cir.setReturnValue(modules);
    }

    @Inject(method = "getCount()I", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(modules.size());
    }

    @Inject(method = "searchTitles(Ljava/lang/String;)Ljava/util/Set;", at = @At("HEAD"), cancellable = true, remap = false)
    private void onSearchTitles(String text, CallbackInfoReturnable<List<Module>> cir) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Comparator.naturalOrder());

        for (Module module : this.modules) {
            int score = Utils.searchLevenshteinDefault(module.title, text, false);
            modules.put(module, modules.getOrDefault(module, 0) + score);
        }

        cir.setReturnValue(modules.keySet().stream().toList());
    }

    @Inject(method = "searchSettingTitles(Ljava/lang/String;)Ljava/util/Set;", at = @At("HEAD"), cancellable = true, remap = false)
    private void onSearchSettingTitles(String text, CallbackInfoReturnable<List<Module>> cir) {
        Map<Module, Integer> modules = new ValueComparableMap<>(Comparator.naturalOrder());

        for (Module module : this.modules) {
            int lowest = Integer.MAX_VALUE;
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int score = Utils.searchLevenshteinDefault(setting.title, text, false);
                    if (score < lowest) lowest = score;
                }
            }
            modules.put(module, modules.getOrDefault(module, 0) + lowest);
        }

        cir.setReturnValue(modules.keySet().stream().toList());
    }

    @Inject(method = "onAction(ZIIZ)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onAction(boolean isKey, int value, int modifiers, boolean isPress, CallbackInfo ci) {
        if (mc.currentScreen != null || Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return;

        for (Module module : modules) {
            if (module.keybind.matches(isKey, value, modifiers) && (isPress || module.toggleOnBindRelease)) {
                module.toggle();
                module.sendToggledMsg();
            }
        }
        ci.cancel();
    }

    @Inject(method = "onOpenScreen(Lmeteordevelopment/meteorclient/events/game/OpenScreenEvent;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onOpenScreen(CallbackInfo ci) {
        if (!Utils.canUpdate()) return;

        for (Module module : modules) {
            if (module.toggleOnBindRelease && module.isActive()) {
                module.toggle();
                module.sendToggledMsg();
            }
        }

        ci.cancel();
    }


}
