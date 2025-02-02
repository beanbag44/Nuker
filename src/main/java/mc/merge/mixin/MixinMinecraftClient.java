package mc.merge.mixin;

import mc.merge.event.EventBus;
import mc.merge.event.events.GameQuitEvent;
import mc.merge.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
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
}