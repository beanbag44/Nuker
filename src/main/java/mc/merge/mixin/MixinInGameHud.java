package mc.merge.mixin;

import mc.merge.event.EventBus;
import mc.merge.event.events.RenderEvent;
import mc.merge.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("TAIL"))
    //? if <1.21 {
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
    //?} else {
    /*private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
    *///?}
        EventBus.INSTANCE.post(new RenderEvent.Render2DEvent(new Renderer2D()));
    }
}