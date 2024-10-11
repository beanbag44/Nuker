package me.beanbag.nuker.mixins.litematica;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SchematicVerifier.class)
public interface ISchematicVerifierAccessor {
    @Accessor(remap = false)
    ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getWrongBlocksPositions();

    @Accessor(remap = false)
    ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getWrongStatesPositions();
}
