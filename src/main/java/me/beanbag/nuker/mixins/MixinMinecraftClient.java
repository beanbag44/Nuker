package me.beanbag.nuker.mixins;

import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow public abstract float getTickDelta();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        EventBus.INSTANCE.post(new TickEvent.Pre(getTickDelta()));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickPost(CallbackInfo ci) {
        EventBus.INSTANCE.post(new TickEvent.Post(getTickDelta()));
    }
}
