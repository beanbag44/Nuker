package mc.merge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mc.merge.event.EventBus;
import mc.merge.event.events.GameQuitEvent;
import mc.merge.event.events.TickEvent;
import mc.merge.event.events.UseItemOnCrossHairTargetEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        EventBus.INSTANCE.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickPost(CallbackInfo ci) {
        EventBus.INSTANCE.post(new TickEvent.Post());
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        EventBus.INSTANCE.post(new GameQuitEvent());
    }

    @ModifyExpressionValue(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult doUseItemOnCrossHairTarget(HitResult original) {
        UseItemOnCrossHairTargetEvent event = new UseItemOnCrossHairTargetEvent();
        EventBus.INSTANCE.post(event);
        if (event.isCanceled()) return null;
        return original;
    }
}