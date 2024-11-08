package me.beanbag.nuker.mixins;

import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.RenderEvent;
import me.beanbag.nuker.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        EventBus.INSTANCE.post(new RenderEvent.Render2DEvent(new Renderer2D()));
    }
}
