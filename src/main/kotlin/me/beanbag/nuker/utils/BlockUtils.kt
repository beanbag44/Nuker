package me.beanbag.nuker.utils

import baritone.api.BaritoneAPI
import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.LitematicaUtils.schematicIncorrectBlockPlacements
import me.beanbag.nuker.utils.LitematicaUtils.schematicIncorrectStatePlacements
import net.minecraft.block.*
import net.minecraft.fluid.WaterFluid
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

object BlockUtils {
    fun getBlockSphere(center: Vec3d, radius: Double): ArrayList<PosAndState> =
        getBlockCube(center, radius).apply {
            removeIf { posAndState ->
                val posVec3d = Vec3d(posAndState.blockPos.x.toDouble(), posAndState.blockPos.y.toDouble(), posAndState.blockPos.z.toDouble())
                val closestPoint = posAndState.blockState.getOutlineShape(mc.world, posAndState.blockPos).getClosestPointTo(center).getOrNull()?.add(posVec3d)
                center.distanceTo(closestPoint ?: posAndState.blockPos.toCenterPos()) > radius
            }
        }

    fun getBlockCube(center: Vec3d, radius: Double): ArrayList<PosAndState> {
        val posList = arrayListOf<PosAndState>()
        val radToInt = radius.toInt()
        val radDecimal = radius - radToInt
        for (x in -radToInt..radToInt) {
            for (y in -radToInt..radToInt) {
                for (z in -radToInt..radToInt) {
                    val xRadDecimal = if (x > 0) radDecimal else -radDecimal
                    val yRadDecimal = if (y > 0) radDecimal else -radDecimal
                    val zRadDecimal = if (z > 0) radDecimal else -radDecimal
                    val pos = BlockPos(
                        (center.x + x + xRadDecimal).toInt(),
                        (center.y + y + yRadDecimal).toInt(),
                        (center.z + z + zRadDecimal).toInt()
                    )
                    mc.world?.let { world ->
                        posList.add(
                            PosAndState(
                                pos,
                                pos.getState(world)
                            )
                        )
                    }
                }
            }
        }
        return posList
    }

    fun sortBlockVolume(posAndStateList: ArrayList<PosAndState>, center: Vec3d, sortStyle: VolumeSort): ArrayList<PosAndState> =
        posAndStateList.apply {
            when (sortStyle) {
                VolumeSort.Closest -> sortBy { center.distanceTo(it.blockPos.toCenterPos()) }
                VolumeSort.Farthest -> sortBy { -center.distanceTo(it.blockPos.toCenterPos()) }
                VolumeSort.TopDown -> sortBy { -it.blockPos.y }
                VolumeSort.BottomUp -> sortBy { it.blockPos.y }
                VolumeSort.Random -> shuffle()
            }
        }

    fun filterBlocksToBreakable(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            mc.world?.let { world ->
                removeIf {
                    val state = it.blockState
                    state.getHardness(world, it.blockPos) == -1f
                            || state.block.hardness == 600f
                            || isStateEmpty(state)
                }
            }
        }

    fun filterLiquidSupportingBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            var scannerPos: BlockPos

            removeIf {
                if (isAdjacentToLiquid(it.blockPos)) return@removeIf true

                scannerPos = it.blockPos.up()

                if (scannerPos.state?.block !is FallingBlock) return@removeIf false

                if (isAdjacentToLiquid(scannerPos)) return@removeIf true

                while (true) {
                    scannerPos = scannerPos.up()

                    if (scannerPos.state?.block !is FallingBlock) return@removeIf false

                    if (isAdjacentToLiquid(scannerPos)) break
                }

                true
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

    fun filterBlocksToBaritoneSelections(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            removeIf {
                !isWithinABaritoneSelection(it.blockPos)
            }
        }

    private fun isWithinABaritoneSelection(pos: BlockPos): Boolean {
        BaritoneAPI.getProvider().allBaritones.forEach {
            it.selectionManager.selections.forEach { sel ->
                if (pos.x >= sel.min().x && pos.x <= sel.max().x
                    && pos.y >= sel.min().y && pos.y <= sel.max().y
                    && pos.z >= sel.min().z && pos.z <= sel.max().z) {
                    return true
                }
            }
        }

        return false
    }

    fun filterCorrectlyPlacedLitematicaBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            removeIf {
                !schematicIncorrectBlockPlacements.contains(it.blockPos)
                        && !schematicIncorrectStatePlacements.contains(it.blockPos)
            }
        }

    fun filterBlocksToCanal(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.apply {
            mc.world?.let { world ->
                removeIf {
                    val pos = it.blockPos
                    val x = pos.x
                    val y = pos.y
                    val z = pos.z
                    val block = it.blockState.block

                    if (z < 0
                        || (y < 59 || x !in -13..12)
                        && (y < 60 || (x != 13 && x != -14))
                        && (y < 62 || (x !in 13..15 && x !in -16..-14))
                    ) {
                        return@removeIf true
                    }

                    if ((x == 13 && y <= 61)
                        || (x == -14 && y <= 61)
                        || (y == 62 && (x == 14 || x == 13))
                        || (y == 62 && (x == -15 || x == -14))
                        && pos.getState(world).block == Blocks.OBSIDIAN) {
                        return@removeIf true
                    }

                    if (y == 59) {
                        val biome = world.getBiome(pos)
                        val isInRiver = biome.isIn(BiomeTags.IS_RIVER)

                        return@removeIf when {
                            isInRiver && block == Blocks.CRYING_OBSIDIAN -> true
                            !isInRiver && block == Blocks.OBSIDIAN -> true
                            else -> false
                        }
                    }

                    return@removeIf (y == 62 && x == 15)
                        || (y == 62 && x == -16)
                        && block == Blocks.CRYING_OBSIDIAN
                }
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

    fun BlockPos.getState(world: World): BlockState =
        world.getBlockState(this)


    fun isBlockSign(pos: BlockPos?): Boolean {

        val block = mc.world?.getBlockState(pos)?.block

        return block is AbstractSignBlock
                || block is AbstractBannerBlock
    }

    fun isNextToSign(pos: BlockPos): Boolean {
        if (isBlockSign(pos.north())) return true
        if (isBlockSign(pos.south())) return true
        if (isBlockSign(pos.east())) return true
        if (isBlockSign(pos.west())) return true
        if (isBlockSign(BlockPos(pos.up()))) return true

        return false
    }
}

fun BlockPos.closestCorner(toPos: Vec3d) : Vec3d {
    val x = if (toPos.x > x) x.toDouble() + 0.001 else x.toDouble() + 0.999
    val y = if (toPos.y > y) y.toDouble() + 0.001 else y.toDouble() + 0.999
    val z = if (toPos.z > z) z.toDouble() + 0.001 else z.toDouble() + 0.999

    return Vec3d(x, y, z)
}