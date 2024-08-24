package me.beanbag.nuker.utils

import fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager
import net.minecraft.util.math.BlockPos

object LitematicaUtils {
    var schematicMismatches = hashSetOf<BlockPos>()

    fun updateSchematicMismatches() {
        schematicMismatches.clear()

        for (placement in getSchematicPlacementManager().allSchematicsPlacements) {
            if (!placement.isEnabled) continue
            schematicMismatches.addAll(placement.schematicVerifier.wrongBlocksPositions.values())
        }
    }
}