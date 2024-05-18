package me.beanbag.mixin;

import me.beanbag.utils.FreeLook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    @Shadow @Final private MinecraftClient client;
    @Inject(method = "updateMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;cursorDeltaX:D", opcode = Opcodes.PUTFIELD, shift = At.Shift.BEFORE), cancellable = true)
    private void updateMouseChangeLookDirection(CallbackInfo ci) {
        if (FreeLook.INSTANCE.isEnabled()) {
            double f = this.client.options.getMouseSensitivity().getValue() * 0.6000000238418579 + 0.20000000298023224;
            double g = f * f * f;
            double h = g * 8.0;
            double k = this.cursorDeltaX * h;
            double l = this.cursorDeltaY * h;
            int m = 1;
            if (this.client.options.getInvertYMouse().getValue()) {
                m = -1;
            }
            float yaw = (float) (FreeLook.INSTANCE.getYaw() + k * 0.15F);
            FreeLook.INSTANCE.setYaw(yaw);
            float pitch = MathHelper.clamp((float) (FreeLook.INSTANCE.getPitch() + (l * (double) m) * 0.15F), -90.0F, 90.0F);
            FreeLook.INSTANCE.setPitch(pitch);
            if (Math.abs(pitch) > 90.0F) {
                yaw = pitch > 0.0F ? 90.0F : -90.0F;
                FreeLook.INSTANCE.setYaw(yaw);
            }
            cursorDeltaX = 0.0;
            cursorDeltaY = 0.0;
            ci.cancel();
        }
    }
}
