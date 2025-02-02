package mc.merge.util

import fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager
import fi.dy.masa.litematica.world.SchematicWorldHandler
import fi.dy.masa.malilib.interfaces.ICompletionListener
import mc.merge.mixin.litematica.ISchematicVerifierAccessor
import net.minecraft.util.math.BlockPos

object LitematicaUtils {
    var schematicIncorrectBlockPlacements = hashSetOf<BlockPos>()
    var schematicIncorrectStatePlacements = hashSetOf<BlockPos>()

    fun InGame.updateSchematicMismatches() {
        schematicIncorrectBlockPlacements.clear()
        schematicIncorrectStatePlacements.clear()

        for (placement in getSchematicPlacementManager().allSchematicsPlacements) {
            if (!placement.isEnabled) continue
            val schematicVerifier = placement.schematicVerifier
            if (!placement.hasVerifier()) {
                schematicVerifier.startVerification(
                    world,
                    SchematicWorldHandler.getSchematicWorld(),
                    placement,
                    VerifierCompletionListener()
                )
            }
            val accessorSchematicVerifier = schematicVerifier as ISchematicVerifierAccessor
            schematicIncorrectBlockPlacements.addAll(accessorSchematicVerifier.wrongBlocksPositions.values())
            schematicIncorrectStatePlacements.addAll(accessorSchematicVerifier.wrongStatesPositions.values())
        }
    }

    class VerifierCompletionListener : ICompletionListener {
        override fun onTaskCompleted() {
            println("Schematic Verification Completed!")
        }

        override fun onTaskAborted() {
            super.onTaskAborted()
            println("Schematic Verification Failed")
        }
    }
}