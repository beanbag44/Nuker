package me.beanbag.nuker.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.nuklear.Nuklear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.beanbag.nuker.render.gui.GUI.INSTANCE;
import static me.beanbag.nuker.render.gui.GUI.ctx;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    @Inject(method = "flipFrame(J)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V", ordinal = 0))
    private static void beforePollEvents(CallbackInfo ci) {
        if (INSTANCE.getNkInitialized()) {
            Nuklear.nk_input_begin(ctx);
        }
    }

    @Inject(method = "flipFrame(J)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V", ordinal = 0, shift = At.Shift.AFTER))
    private static void afterPollEvents(CallbackInfo ci) {
        if (INSTANCE.getNkInitialized()) {
            Nuklear.nk_input_end(ctx);
        }
    }
}