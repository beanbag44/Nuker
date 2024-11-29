package me.beanbag.nuker.mixins;

import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.KeyEvent;
import me.beanbag.nuker.types.KeyAction;
import net.minecraft.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            KeyEvent event = new KeyEvent(key, modifiers, KeyAction.Companion.get(action));
            EventBus.INSTANCE.post(event);
            if (event.isCanceled()) ci.cancel();
        }
    }
}
