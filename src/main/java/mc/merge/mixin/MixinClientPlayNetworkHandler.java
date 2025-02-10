package mc.merge.mixin;

import mc.merge.event.EventBus;
import mc.merge.event.events.GameJoinedEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo ci) {
        EventBus.INSTANCE.post(new GameJoinedEvent());
    }
}
