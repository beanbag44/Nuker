package mc.merge.mixin;

import mc.merge.command.ExecutableClickEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "handleTextClick", at = @At("HEAD"), cancellable = true)
    private void handleTextClick(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() instanceof ExecutableClickEvent event) {
            event.getOnClick().invoke();
            cir.setReturnValue(true);
        }
    }
}
