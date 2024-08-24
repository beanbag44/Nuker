package me.beanbag.nuker.utils

import fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager
import net.minecraft.util.math.BlockPos

object LitematicaUtils {
    var schematicIncorrectBlockPlacements = hashSetOf<BlockPos>()
    var schematicIncorrectStatePlacements = hashSetOf<BlockPos>()

    fun updateSchematicMismatches() {
        schematicIncorrectBlockPlacements.clear()
        schematicIncorrectStatePlacements.clear()

        for (placement in getSchematicPlacementManager().allSchematicsPlacements) {
            if (!placement.isEnabled) continue
            schematicIncorrectBlockPlacements.addAll(placement.schematicVerifier.wrongBlocksPositions.values())
            schematicIncorrectStatePlacements.addAll(placement.schematicVerifier.wrongStatesPositions.values())
        }
    }
}