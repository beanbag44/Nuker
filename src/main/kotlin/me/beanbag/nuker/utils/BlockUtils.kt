package me.beanbag.nuker.utils

import me.beanbag.nuker.Loader.Companion.mc
import me.beanbag.nuker.modules.Nuker.radius
import me.beanbag.nuker.modules.Nuker.shape
import me.beanbag.nuker.settings.enumsettings.VolumeShape
import me.beanbag.nuker.types.PosAndState
import net.minecraft.block.BlockState
import net.minecraft.fluid.WaterFluid
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
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
                it.blockState?.run {
                    this.getHardness(mc.world, it.blockPos) == -1f
                            || this.block.hardness == 600f
                            || isStateEmpty(this)
                } ?: true
            }
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