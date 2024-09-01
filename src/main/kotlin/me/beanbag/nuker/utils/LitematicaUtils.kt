package me.beanbag.nuker.utils

import fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager
import me.beanbag.nuker.mixins.litematica.ISchematicVerifierAccessor
import net.minecraft.util.math.BlockPos

object LitematicaUtils {
    var schematicIncorrectBlockPlacements = hashSetOf<BlockPos>()
    var schematicIncorrectStatePlacements = hashSetOf<BlockPos>()

    fun updateSchematicMismatches() {
        schematicIncorrectBlockPlacements.clear()
        schematicIncorrectStatePlacements.clear()

        for (placement in getSchematicPlacementManager().allSchematicsPlacements) {
            if (!placement.isEnabled) continue
            val schematicVerifier = placement.schematicVerifier as ISchematicVerifierAccessor
            schematicIncorrectBlockPlacements.addAll(schematicVerifier.wrongBlocksPositions.values())
            schematicIncorrectStatePlacements.addAll(schematicVerifier.wrongStatesPositions.values())
        }
    }
}