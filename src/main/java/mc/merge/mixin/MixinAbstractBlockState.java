package mc.merge.mixin;

import mc.merge.event.EventBus;
import mc.merge.event.events.UseBlockEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.block.AbstractBlock$AbstractBlockState")
public class MixinAbstractBlockState {
    @Inject(method = "onUse", at = @At("HEAD"))
    private void onUse(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
//        if (ModCore.INSTANCE.getMc().world.getBlockState(hit.getBlockPos()).getBlock() instanceof EnderChestBlock) {
//            return;
//        }
        EventBus.INSTANCE.post(new UseBlockEvent(hand, hit));
    }
}
