package mc.merge.mixin;

import mc.merge.event.EventBus;
import mc.merge.event.events.BlockUpdateEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "handleBlockUpdate", at = @At("HEAD"))
    private void onBlockUpdateTail(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        EventBus.INSTANCE.post(new BlockUpdateEvent(pos, state));
    }
}
