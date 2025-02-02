package mc.merge.mixin;

import mc.merge.handler.RotationHandler;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public class MixinCamera {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        if (RotationHandler.INSTANCE.getFreeLooking()) {
            args.set(0, RotationHandler.INSTANCE.getYaw());
            args.set(1, RotationHandler.INSTANCE.getPitch());
        }
    }
}
