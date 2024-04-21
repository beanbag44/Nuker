package me.beanbag.mixin.litematica;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SchematicVerifier.class)
public interface ISchematicVerifierAccessor {
    @Accessor
    List<BlockPos> getMismatchedBlocksPositionsClosest();

    @Accessor
    ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getWrongBlocksPositions();

    @Accessor
    ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> getMissingBlocksPositions();
}
