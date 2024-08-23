package me.beanbag.nuker.utils

import me.beanbag.nuker.Nuker.mc
import me.beanbag.nuker.Nuker.radius
import me.beanbag.nuker.Nuker.shape
import me.beanbag.nuker.settings.enumsettings.VolumeShape
import me.beanbag.nuker.types.PosAndState
import net.minecraft.block.BlockState
import net.minecraft.block.FallingBlock
import net.minecraft.fluid.WaterFluid
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.ArrayList

object BlockUtils {
    fun getBlockVolume(): ArrayList<PosAndState> =
        if (shape == VolumeShape.Sphere) getBlockSphere() else getBlockCube()

    private fun getBlockSphere(): ArrayList<PosAndState> =
        getBlockCube().apply {
            removeIf { posAndState ->
                mc.player!!.eyePos.distanceTo(posAndState.blockPos.toCenterPos()) > radius
            }
        }

    private fun getBlockCube(): ArrayList<PosAndState> {
        val posList = arrayListOf<PosAndState>()
        val radToInt = radius.toInt()
        val radDecimal = radius - radToInt
        val eyePos = mc.player!!.eyePos
        for (x in -radToInt..radToInt) {
            for (y in -radToInt..radToInt) {
                for (z in -radToInt..radToInt) {
                    val xRadDecimal = if (x > 0) radDecimal else -radDecimal
                    val yRadDecimal = if (y > 0) radDecimal else -radDecimal
                    val zRadDecimal = if (z > 0) radDecimal else -radDecimal
                    val pos = BlockPos(
                        (eyePos.x + x + xRadDecimal).toInt(),
                        (eyePos.y + y + yRadDecimal).toInt(),
                        (eyePos.z + z + zRadDecimal).toInt()
                    )
                    posList.add(
                        PosAndState(
                            pos,
                            pos.state
                        )
                    )
                }
            }
        }
        return posList
    }

    fun filterUnbreakableBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            removeIf {
                it.blockState?.let { state ->
                    state.getHardness(mc.world, it.blockPos) == -1f
                            || state.block.hardness == 600f
                            || isStateEmpty(state)
                } ?: true
            }
        }

    fun filterImpossibleFlattenBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            mc.player?.let { player ->
                val playerPos = player.blockPos
                val flattenLevel = if (crouchLowersFlatten && player.isSneaking) {
                    playerPos.y - 1
                } else {
                    playerPos.y
                }

                if (!flattenMode.isSmart()) {
                    removeIf {
                        it.blockPos.y < flattenLevel
                    }
                    return@apply
                }

                val playerLookDir = mc.player?.horizontalFacing
                val smartFlattenDir = if (flattenMode == FlattenMode.Smart) {
                    playerLookDir
                } else {
                    playerLookDir?.opposite
                }

                removeIf {
                    if (it.blockPos.y >= flattenLevel) return@removeIf false

                    val zeroedPos = it.blockPos.add(-playerPos.x, -playerPos.y, -playerPos.z)

                    return@removeIf (zeroedPos.x  >= 0 && smartFlattenDir == Direction.EAST)
                            || (zeroedPos.z >= 0 && smartFlattenDir == Direction.SOUTH)
                            || (zeroedPos.x <= 0 && smartFlattenDir == Direction.WEST)
                            || (zeroedPos.z <= 0 && smartFlattenDir == Direction.NORTH)
                }
            }
        }

    fun filterLiquidAffectingBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            mc.world?.let { world ->
                val cachedGravityBlocks = hashSetOf<BlockPos>()
                var scannerPos: BlockPos

                removeIf {
                    if (cachedGravityBlocks.contains(it.blockPos)) {
                        return@removeIf false
                    }

                    if (isAdjacentToLiquid(it.blockPos)) return@removeIf true

                    scannerPos = it.blockPos.up()

                    if (scannerPos.state?.block !is FallingBlock) return@removeIf false

                    cachedGravityBlocks.add(scannerPos)

                    if (isAdjacentToLiquid(scannerPos)) return@removeIf true

                    while (true) {
                        scannerPos = scannerPos.up()

                        if (scannerPos.state?.block !is FallingBlock) return@removeIf false

                        cachedGravityBlocks.add(scannerPos)

                        if (isAdjacentToLiquid(scannerPos)) break
                    }

                    return@removeIf true
                }
            }
        }

    private fun isAdjacentToLiquid(blockPos: BlockPos): Boolean {
        mc.world?.let { world ->
            Direction.entries.forEach { direction ->
                if (direction == Direction.DOWN) return@forEach

                if (!blockPos.offset(direction).getState(world).fluidState.isEmpty) return true
            }

            return false
        } ?: return false
    }

    fun isBlockBroken(currentState: BlockState?, newState: BlockState): Boolean {
        currentState?.let { current ->
            if (isStateEmpty(current)) return false

            return (newState.isAir && (
                    !current.properties.contains(Properties.WATERLOGGED)
                            || !current.get(Properties.WATERLOGGED)
                            )
                    ) || (
                    newState.fluidState.fluid is WaterFluid
                            && !newState.properties.contains(Properties.WATERLOGGED)
                            && current.properties.contains(Properties.WATERLOGGED)
                            && current.get(Properties.WATERLOGGED)
                    )
        } ?: return false
    }

    private fun isStateEmpty(state: BlockState) =
        state.isAir || (
                (!state.properties.contains(Properties.WATERLOGGED)
                        || !state.get(Properties.WATERLOGGED))
                        && !state.fluidState.isEmpty
                )

    val BlockPos.state
        get() = mc.world?.getBlockState(this)

    fun BlockPos.getState(world: World): BlockState =
        world.getBlockState(this)
}