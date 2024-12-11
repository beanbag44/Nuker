package me.beanbag.nuker.mixins;

import me.beanbag.nuker.ModConfigs;
import me.beanbag.nuker.eventsystem.EventBus;
import me.beanbag.nuker.eventsystem.events.PlayerMoveEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        //noinspection ConstantValue
        if ((Object) this == ModConfigs.INSTANCE.getMc().player) {
            EventBus.INSTANCE.post(new PlayerMoveEvent());
        }
    }
}
