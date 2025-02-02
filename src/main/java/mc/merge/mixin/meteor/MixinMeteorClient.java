package mc.merge.mixin.meteor;

import mc.merge.Loader;
import mc.merge.ModCore;
import meteordevelopment.meteorclient.MeteorClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeteorClient.class)
public class MixinMeteorClient {

    @Inject(method = "onInitializeClient", at = @At("TAIL"), remap = false)
    public void onInitialize(CallbackInfo ci) {
        ModCore.INSTANCE.setMeteorIsLoaded(true);
        Loader.Companion.tryInitialize();
        System.out.println("Trying to initialize from MixinMeteorClient");
    }
}