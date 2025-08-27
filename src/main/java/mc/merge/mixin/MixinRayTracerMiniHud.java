package mc.merge.mixin;


import fi.dy.masa.minihud.util.RayTracer;
import mc.merge.duck.IRayTracerDuck;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(RayTracer.class)
public class MixinRayTracerMiniHud implements IRayTracerDuck {

    @Shadow
    protected int blockX;

    @Shadow
    protected int blockY;

    @Shadow
    protected int blockZ;

    @Unique
    @Override
    public BlockPos stonecutter_nuker$getBlockPos() {
        return new BlockPos(this.blockX,this.blockY,this.blockZ);
    }
}
