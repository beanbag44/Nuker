package me.beanbag.nuker.utils

import me.beanbag.nuker.Nuker.mc
import me.beanbag.nuker.Nuker.radius
import me.beanbag.nuker.Nuker.shape
import me.beanbag.nuker.settings.VolumeShape
import net.minecraft.block.BlockState
import net.minecraft.fluid.WaterFluid
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos

object BlockUtils {
    fun getBlockVolume(): List<BlockPos> =
        if (shape == VolumeShape.Sphere) getBlockSphere() else getBlockCube()

    private fun getBlockSphere(): List<BlockPos> =
        getBlockCube().apply {
            removeIf { pos ->
                mc.player!!.eyePos.distanceTo(pos.toCenterPos()) > radius
            }
        }

    private fun getBlockCube(): MutableList<BlockPos> {
        val posList = mutableListOf<BlockPos>()
        val radToInt = radius.toInt()
        val radDecimal = radius - radToInt
        val eyePos = mc.player!!.eyePos
        for (x in -radToInt..radToInt) {
            for (y in -radToInt..radToInt) {
                for (z in -radToInt..radToInt) {
                    val xRadDecimal = if (x > 0) radDecimal else -radDecimal
                    val yRadDecimal = if (y > 0) radDecimal else -radDecimal
                    val zRadDecimal = if (z > 0) radDecimal else -radDecimal
                    posList.add(
                        BlockPos(
                            (eyePos.x + x + xRadDecimal).toInt(),
                            (eyePos.y + y + yRadDecimal).toInt(),
                            (eyePos.z + z + zRadDecimal).toInt()
                        )
                    )
                }
            }
        }
        return posList
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

}