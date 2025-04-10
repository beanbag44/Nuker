package mc.merge.util

import fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement
import fi.dy.masa.litematica.selection.Box
import fi.dy.masa.litematica.world.SchematicWorldHandler
import fi.dy.masa.malilib.interfaces.ICompletionListener
import mc.merge.mixin.litematica.ISchematicVerifierAccessor
import net.minecraft.util.math.BlockPos
import kotlin.math.max
import kotlin.math.min


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

    fun InGame.checkSchematicState(pos: BlockPos): SchematicState {
        val schematicWorld = SchematicWorldHandler.getSchematicWorld()
        val schematicState = schematicWorld?.getBlockState(pos)
        val worldState = world.getBlockState(pos)

        if (!isBlockInAnySchematics(pos)) {
            return SchematicState.Unknown
        }
        return if (schematicState == worldState || schematicState?.isAir == true && worldState.isAir) {
            SchematicState.Correct
        } else if (worldState.isAir) {
            SchematicState.Missing
        } else if (schematicState?.block !== worldState.block) {
            SchematicState.WrongBlock
        } else {
            SchematicState.WrongState
        }
    }

    private fun isBlockInAnySchematics(pos: BlockPos): Boolean {
        for (placement in getSchematicPlacementManager().allSchematicsPlacements) {
            if (!placement.isEnabled) continue

            for (box in placement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.ANY).values) {
                if (isBlockPosWithin(pos, box)) return true
            }
        }
        return false
    }

    private fun isBlockPosWithin(pos: BlockPos, box: Box?): Boolean {
        val min = box?.min()
        val max = box?.max()
        if (min == null || max == null) return false
        return pos.x in min.x..max.x
                && pos.y in min.y..max.y
                && pos.z in min.z..max.z
    }

    fun Box.min(): BlockPos {
        val pos1 = this.pos1 ?: BlockPos.ORIGIN
        val pos2 = this.pos2 ?: BlockPos.ORIGIN
        return BlockPos(
            min(pos1.x, pos2.x),
            min(pos1.y, pos2.y),
            min(pos1.z, pos2.z)
        )
    }

    fun Box.max(): BlockPos {
        val pos1 = this.pos1 ?: BlockPos.ORIGIN
        val pos2 = this.pos2 ?: BlockPos.ORIGIN
        return BlockPos(
            max(pos1.x, pos2.x),
            max(pos1.y, pos2.y),
            max(pos1.z, pos2.z)
        )
    }

    enum class SchematicState {
        WrongBlock,
        WrongState,
        Missing,
        Correct,
        Unknown,
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