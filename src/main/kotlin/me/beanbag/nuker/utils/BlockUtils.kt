package me.beanbag.nuker.utils

import baritone.api.BaritoneAPI
import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.module.modules.nuker.enumsettings.FlattenMode
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.VolumeSort
import me.beanbag.nuker.utils.LitematicaUtils.schematicIncorrectBlockPlacements
import me.beanbag.nuker.utils.LitematicaUtils.schematicIncorrectStatePlacements
import net.minecraft.block.*
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.LavaFluid
import net.minecraft.fluid.WaterFluid
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.state.property.Properties
import net.minecraft.util.math.*
import net.minecraft.world.World
import java.util.*

object BlockUtils {
    const val PLAYER_EYE_HEIGHT = 1.62
    const val PLAYER_CROUCHING_EYE_HEIGHT = PLAYER_EYE_HEIGHT - 0.125

    fun getBlockSphere(center: Vec3d, radius: Double): ArrayList<PosAndState> =
        getBlockCube(center, radius ).apply {
            removeIf { posAndState -> posAndState.blockState.isAir || !canReach(center, posAndState, radius) }
        }

    fun getBlockCube(center: Vec3d, radius: Double): ArrayList<PosAndState> {
        val posList = arrayListOf<PosAndState>()
        val min = BlockPos((center.x - radius).toInt(), (center.y - radius).toInt(), (center.z - radius).toInt())
        val max = BlockPos((center.x + radius).toInt(), (center.y + radius).toInt(), (center.z + radius).toInt())

        allPosInBounds(min, max).map { pos ->
            mc.world?.let { world ->
                posList.add(PosAndState(pos, pos.getState(world)))
            }
        }
        return posList
    }

    fun canReach(from:Vec3d, block:PosAndState, reach:Double): Boolean {
        return mc.world?.let { world ->
            var closestPoint: Vec3d? = null
            block.blockState.getOutlineShape(world, block.blockPos).boundingBoxes.forEach { box: Box? ->
                if (box == null) return@forEach
                val x = MathHelper.clamp(from.getX(), block.blockPos.x + box.minX, block.blockPos.x + box.maxX)
                val y = MathHelper.clamp(from.getY(), block.blockPos.y + box.minY, block.blockPos.y + box.maxY)
                val z = MathHelper.clamp(from.getZ(), block.blockPos.z + box.minZ, block.blockPos.z + box.maxZ)
                if (closestPoint == null || from.squaredDistanceTo(x, y, z) < from.squaredDistanceTo(closestPoint)) {
                    closestPoint = Vec3d(x, y, z)
                }
            }

            if (closestPoint == null) return false
            return from.distanceTo(closestPoint) <= reach
        } ?: false
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

    fun getValidStandingSpots(min: BlockPos, max: BlockPos): List<BlockPos> {
        return mc.world?.let { world ->
            allPosInBounds(min, max).filter { pos ->
                val stateAbove = world.getBlockState(pos.up())
                val state = world.getBlockState(pos)
                val stateBelow = world.getBlockState(pos.down())
                return@filter canWalkThrough(PosAndState(pos.up(), stateAbove))
                            && canWalkThrough(PosAndState(pos, state))
                            && stateBelow.isFullCube(world, pos)
                        || canWalkThrough(PosAndState(pos.up(), stateAbove))
                            && state.block == Blocks.WATER
                            && stateBelow.block == Blocks.WATER
                        || canWalkThrough(PosAndState(pos.up(), stateAbove))
                            && state.block == Blocks.WATER
                            && stateBelow.isFullCube(world, pos)
            }
        } ?: listOf()
    }

    fun canWalkThrough(block: PosAndState): Boolean {
        if (block.blockState.isAir) return true
        if (block.blockState.block is FlowerBlock) return true
        if (block.blockState.block is TallPlantBlock) return true
        if (block.blockState.block is ShortPlantBlock) return true
        if (block.blockState.block is MushroomPlantBlock) return true
        return false
    }

    fun filterLiquidSupportingBlocks(posAndStateList: ArrayList<PosAndState>) =
        posAndStateList.removeIf { willReleaseLiquids(it) }

    fun filterBlocksToFlatten(
        posAndStateList: ArrayList<PosAndState>,
        crouchLowersFlatten: Boolean,
        flattenMode: FlattenMode
    ) =
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

                val playerLookDir = player.horizontalFacing
                val smartFlattenDir = if (flattenMode == FlattenMode.Smart) {
                    playerLookDir
                } else {
                    playerLookDir?.opposite
                }

                removeIf {
                    if (it.blockPos.y >= flattenLevel) return@removeIf false

                    val zeroedPos = it.blockPos.add(-playerPos.x, -playerPos.y, -playerPos.z)

                    return@removeIf (zeroedPos.x >= 0 && smartFlattenDir == Direction.EAST)
                            || (zeroedPos.z >= 0 && smartFlattenDir == Direction.SOUTH)
                            || (zeroedPos.x <= 0 && smartFlattenDir == Direction.WEST)
                            || (zeroedPos.z <= 0 && smartFlattenDir == Direction.NORTH)
                }
            }
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

    fun isStateEmpty(state: BlockState) =
        state.isAir || (
                (!state.properties.contains(Properties.WATERLOGGED)
                        || !state.get(Properties.WATERLOGGED))
                        && !state.fluidState.isEmpty
                )

    val BlockPos.state
        get() = mc.world?.getBlockState(this)

    fun BlockPos.getState(world: World): BlockState =
        world.getBlockState(this)


    fun isSignOrBanner(pos: BlockPos): Boolean {
        val block = mc.world?.getBlockState(pos)?.block
        return block is AbstractSignBlock || block is AbstractBannerBlock
    }

    private fun willReleaseAdjacentLiquids(block:PosAndState) : Boolean {
        mc.world?.let { world ->
            for(direction in Direction.entries) {
                if (direction == Direction.DOWN) continue

                val adjacentPos = block.blockPos.add(direction.vector)
                val adjacent = PosAndState.from(adjacentPos, world)
                val fluidState = adjacent.blockState.fluidState
                val fluid = fluidState.fluid

                if (fluidState.isEmpty || fluid !is FlowableFluid) continue

                if (direction == Direction.UP) return true

                if (adjacent.blockState.block is Waterloggable && !fluidState.isEmpty) { return true }

                val levelDecreasePerBlock =
                    when (fluid) {
                        is WaterFluid -> fluid.getLevelDecreasePerBlock(world)
                        is LavaFluid -> fluid.getLevelDecreasePerBlock(world)
                        else -> 0
                    }

                if (fluidState.level - levelDecreasePerBlock > 0) {
                    return true
                }
            }
        }
        return false
    }


    fun willReleaseLiquids(block: PosAndState): Boolean {
        val blocksThatWillUpdate = blocksThatWillUpdate(block)
        return blocksThatWillUpdate.any { willReleaseAdjacentLiquids(it) }
    }

    private fun isSupportingAdjacentSignOrBanner(pos: BlockPos): Boolean {
        mc.world?.let { world ->
            for (direction in Direction.entries) {
                val adjacentPos = pos.add(direction.vector)
                val state = world.getBlockState(adjacentPos)
                val block = state.block
                if (block is AbstractSignBlock) {
                    if (block is SignBlock && direction == Direction.UP) {
                        return true
                    } else if (block is WallSignBlock && direction == (state.get(WallBannerBlock.FACING) as Direction)) {
                        return true
                    } else if (block is HangingSignBlock && direction == Direction.DOWN) {
                        return true
                    }
                    //Note, we don't check WallHangingSign because it will persist if the "supporting" block is destroyed
                }
                if (block is AbstractBannerBlock) {
                    if (block is WallBannerBlock && direction == (state.get(WallBannerBlock.FACING) as Direction)) {
                        return true
                    } else if (block is BannerBlock && direction == Direction.UP) {
                        return  true
                    }
                }
            }
        }
       return false
    }

    fun blocksThatWillUpdate(block:PosAndState) : List<PosAndState> {
        val blocksThatWillUpdate = hashSetOf<PosAndState>()
        val checkQueue = hashSetOf(block)

        mc.world?.let { world ->
            while(checkQueue.isNotEmpty()) {
                val currentPos = checkQueue.first()
                checkQueue.remove(currentPos)
                blocksThatWillUpdate.add(currentPos)
                for (direction in Direction.entries) {
                    val adjacentPos = currentPos.blockPos.add(direction.vector)
                    val adjacent = PosAndState.from(adjacentPos, world)
                    if (adjacent.blockState.block is FallingBlock) {
                        if (blocksThatWillUpdate.contains(adjacent)) {
                            continue
                        }
                        if (direction == Direction.UP) {
                            checkQueue.add(adjacent)
                        } else if (FallingBlock.canFallThrough(world.getBlockState(adjacentPos.down()))) {
                            checkQueue.add(adjacent)
                        }
                    }
                }
            }
        }

        return blocksThatWillUpdate.toList()
    }

    fun isSupportingSignOrBanner(block: PosAndState): Boolean =
        blocksThatWillUpdate(block).any { isSupportingAdjacentSignOrBanner(it.blockPos) }

    fun allPosInBounds(pos1: BlockPos, pos2: BlockPos): List<BlockPos> = forEachXYZ(pos1, pos2) { BlockPos(it) }

    private fun <T> forEachXYZ(min: Vec3i, max: Vec3i, action: (Vec3i) -> T): List<T> =
        (min.x..max.x).flatMap { x ->
            (min.y..max.y).flatMap { y ->
                (min.z..max.z).map { z ->
                    action(Vec3i(x, y, z))
                }
            }
        }
}

fun BlockPos.closestCorner(toPos: Vec3d) : Vec3d {
    val x = if (toPos.x > x) x.toDouble() + 0.001 else x.toDouble() + 0.999
    val y = if (toPos.y > y) y.toDouble() + 0.001 else y.toDouble() + 0.999
    val z = if (toPos.z > z) z.toDouble() + 0.001 else z.toDouble() + 0.999

    return Vec3d(x, y, z)
}