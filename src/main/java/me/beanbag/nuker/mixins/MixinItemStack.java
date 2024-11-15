package me.beanbag.nuker.mixins;

import me.beanbag.nuker.ModConfigs;
import me.beanbag.nuker.module.modules.EquipmentSaver;
import me.beanbag.nuker.utils.InGameKt;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @ModifyArg(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z"), index = 0)
    private int hookDamage(int amount) {
        EquipmentSaver equipmentSaver = ModConfigs.INSTANCE.getModuleByClass(EquipmentSaver.class);
        if (equipmentSaver != null && equipmentSaver.getEnabled()) {
            InGameKt.runInGame(inGame -> {
                equipmentSaver.onItemDamaged(inGame, ((ItemStack) (Object) this));
                return null;
            });
        }
        return amount;
    }
}
