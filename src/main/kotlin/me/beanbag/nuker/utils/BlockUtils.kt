package me.beanbag.nuker.utils

import baritone.api.BaritoneAPI
import me.beanbag.nuker.ModConfigs.mc
import me.beanbag.nuker.module.modules.nuker.enumsettings.FlattenMode
import me.beanbag.nuker.types.PosAndState
import me.beanbag.nuker.types.VolumeSort
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
    const val PLAYER_EYE_HEIGHT: Float = 1.62f
    const val PLAYER_CROUCHING_EYE_HEIGHT: Float = PLAYER_EYE_HEIGHT - 0.125f
    const val NEGATIVE_AXIS_FIX = 1

    fun InGame.getBlockSphere(
        center: Vec3d,
        radius: Double,
        removeIf: ((BlockPos, BlockState) -> Boolean)?
    ): ArrayList<PosAndState> =
        getBlockCube(center, radius, removeIf).apply {
            removeIf { !canReach(center, it, radius) }
        }

    fun InGame.getBlockCube(
        center: Vec3d,
        radius: Double,
        removeIf: ((BlockPos, BlockState) -> Boolean)?
    ): ArrayList<PosAndState> {
        val posList = arrayListOf<PosAndState>()
        val min = BlockPos(
            (center.x - radius).toInt() - NEGATIVE_AXIS_FIX,
            (center.y - radius).toInt() - NEGATIVE_AXIS_FIX,
            (center.z - radius).toInt() - NEGATIVE_AXIS_FIX
        )
        val max = BlockPos(
            (center.x + radius).toInt(),
            (center.y + radius).toInt(),
            (center.z + radius).toInt()
        )

        allPosInBounds(min, max).forEach { pos ->
            val blockState = pos.getState(world)
            if (removeIf?.invoke(pos, blockState) != true) {
                posList.add(PosAndState(pos, blockState))
            }
        }
        return posList
    }

    fun InGame.canReach(from:Vec3d, block:PosAndState, reach:Double): Boolean {
        var closestPoint: Vec3d? = null
        block.blockState.getOutlineShape(world, block.blockPos).boundingBoxes.forEach { box ->
            if (box == null) return@forEach
            val x = MathHelper.clamp(
                from.getX(),
                block.blockPos.x + box.minX,
                block.blockPos.x + box.maxX
            )
            val y = MathHelper.clamp(
                from.getY(),
                block.blockPos.y + box.minY,
                block.blockPos.y + box.maxY
            )
            val z = MathHelper.clamp(
                from.getZ(),
                block.blockPos.z + box.minZ,
                block.blockPos.z + box.maxZ
            )
            if (closestPoint == null || from.squaredDistanceTo(x, y, z) < from.squaredDistanceTo(closestPoint)) {
                closestPoint = Vec3d(x, y, z)
            }
        }

        if (closestPoint == null) return false
        return from.distanceTo(closestPoint) <= reach
    }

    fun sortBlockVolume(
        posAndStateList: ArrayList<PosAndState>,
        center: Vec3d,
        sortStyle: VolumeSort
    ): ArrayList<PosAndState> =
        posAndStateList.apply {
            when (sortStyle) {
                VolumeSort.Closest -> sortBy { center.distanceTo(it.blockPos.toCenterPos()) }
                VolumeSort.Farthest -> sortBy { -center.distanceTo(it.blockPos.toCenterPos()) }
                VolumeSort.TopDown -> sortBy { -it.blockPos.y }
                VolumeSort.BottomUp -> sortBy { it.blockPos.y }
                VolumeSort.Random -> shuffle()
            }
        }

    fun InGame.isBlockBreakable(pos: BlockPos, state: BlockState): Boolean = state.getHardness(world, pos) != -1f
            && state.block.hardness != 600f
            && !isStateEmpty(state)

    fun InGame.getValidStandingSpots(min: BlockPos, max: BlockPos): List<BlockPos> =
        allPosInBounds(min, max).filter { pos ->
            val stateAbove = world.getBlockState(pos.up())
            val state = world.getBlockState(pos)
            val stateBelow = world.getBlockState(pos.down())
            return@filter canWalkThrough(stateAbove)
                    && stateBelow.isFullCube(world, pos)
                    || canWalkThrough(stateAbove)
                    && state.block == Blocks.WATER
                    && stateBelow.block == Blocks.WATER
                    || canWalkThrough(stateAbove)
                    && state.block == Blocks.WATER
                    && stateBelow.isFullCube(world, pos)
        }

    fun canWalkThrough(state: BlockState): Boolean {
        if (state.isAir) return true
        if (state.block is FlowerBlock) return true
        if (state.block is TallPlantBlock) return true
        if (state.block is ShortPlantBlock) return true
        if (state.block is MushroomPlantBlock) return true
        return false
    }

    fun InGame.isBlockInFlatten(pos: BlockPos, crouchLowersFlatten: Boolean, flattenMode: FlattenMode): Boolean {
        val playerPos = player.blockPos
        val flattenLevel = if (crouchLowersFlatten && player.isSneaking) {
            playerPos.y - 1
        } else {
            playerPos.y
        }

        if (!flattenMode.isSmart()) {
            if (pos.y < flattenLevel) {
                return false
            }
        }

        val playerLookDir = player.horizontalFacing
        val smartFlattenDir = if (flattenMode == FlattenMode.Smart) {
            playerLookDir
        } else {
            playerLookDir?.opposite
        }

        if (pos.y >= flattenLevel) return true

        val zeroedPos = pos.add(-playerPos.x, -playerPos.y, -playerPos.z)

        return (zeroedPos.x < 0 && smartFlattenDir == Direction.EAST)
                || (zeroedPos.z < 0 && smartFlattenDir == Direction.SOUTH)
                || (zeroedPos.x > 0 && smartFlattenDir == Direction.WEST)
                || (zeroedPos.z > 0 && smartFlattenDir == Direction.NORTH)
    }

    fun isWithinABaritoneSelection(pos: BlockPos): Boolean {
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

    fun InGame.isValidCanalBlock(pos: BlockPos, state: BlockState): Boolean {
        val x = pos.x
        val y = pos.y
        val z = pos.z
        val block = state.block

        if (z < 0
            || (y < 59 || x !in -13..12)
            && (y < 60 || (x != 13 && x != -14))
            && (y < 62 || (x !in 13..15 && x !in -16..-14))
        ) {
            return true
        }

        if ((x == 13 && y <= 61)
            || (x == -14 && y <= 61)
            || (y == 62 && (x == 14 || x == 13))
            || (y == 62 && (x == -15 || x == -14))
            && pos.getState(world).block == Blocks.OBSIDIAN) {
            return true
        }

        if (y == 59) {
            val biome = world.getBiome(pos)
            val isInRiver = biome.isIn(BiomeTags.IS_RIVER)

            return when {
                isInRiver && block == Blocks.CRYING_OBSIDIAN -> true
                !isInRiver && block == Blocks.OBSIDIAN -> true
                else -> false
            }
        }

        return ((y == 62 && x == 15)
                || (y == 62 && x == -16))
                && block == Blocks.CRYING_OBSIDIAN
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

    private fun InGame.willReleaseAdjacentLiquids(block: BlockPos) : Boolean {
        for(direction in Direction.entries) {
            if (direction == Direction.DOWN) continue

            val adjacentPos = block.add(direction.vector)
            val adjacent = world.getBlockState(adjacentPos)
            val fluidState = adjacent.fluidState
            val fluid = fluidState.fluid

            if (fluidState.isEmpty || fluid !is FlowableFluid) continue

            if (direction == Direction.UP) return true

            if (adjacent.block is Waterloggable && !fluidState.isEmpty) { return true }

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
        return false
    }


    fun InGame.willReleaseLiquids(pos: BlockPos): Boolean {
        val blocksThatWillUpdate = blocksThatWillUpdate(pos)
        return blocksThatWillUpdate.any { willReleaseAdjacentLiquids(it.blockPos) }
    }

    private fun InGame.isSupportingAdjacentSignOrBanner(pos: BlockPos): Boolean {
        for (direction in Direction.entries) {
            val adjacentPos = pos.add(direction.vector)
            val state = world.getBlockState(adjacentPos)
            val block = state.block
            if (block is AbstractSignBlock) {
                if (block is SignBlock && direction == Direction.UP) {
                    return true
                } else if (block is WallSignBlock
                    && direction == (state.get(WallBannerBlock.FACING) as Direction)) {
                    return true
                } else if (block is HangingSignBlock
                    && direction == Direction.DOWN) {
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
       return false
    }

    fun InGame.blocksThatWillUpdate(pos: BlockPos): List<PosAndState> {
        val blocksThatWillUpdate = hashSetOf<PosAndState>()

        val checkQueue = hashSetOf(pos)

        while(checkQueue.isNotEmpty()) {
            val currentPos = checkQueue.first()
            checkQueue.remove(currentPos)
            blocksThatWillUpdate.add(PosAndState.from(currentPos, world))
            for (direction in Direction.entries) {
                val adjacentPos = currentPos.add(direction.vector)
                val adjacent = PosAndState.from(adjacentPos, world)
                if (adjacent.blockState.block is FallingBlock) {
                    if (blocksThatWillUpdate.contains(adjacent)) {
                        continue
                    }
                    if (direction == Direction.UP) {
                        checkQueue.add(adjacent.blockPos)
                    } else if (FallingBlock.canFallThrough(world.getBlockState(adjacentPos.down()))) {
                        checkQueue.add(adjacent.blockPos)
                    }
                }
            }
        }

        return blocksThatWillUpdate.toList()
    }

    fun InGame.isSupportingSignOrBanner(pos: BlockPos): Boolean =
        blocksThatWillUpdate(pos).any { isSupportingAdjacentSignOrBanner(it.blockPos) }

    fun allPosInBounds(pos1: BlockPos, pos2: BlockPos): List<BlockPos> = forEachXYZ(pos1, pos2) { BlockPos(it) }

    private fun <T> forEachXYZ(min: Vec3i, max: Vec3i, action: (Vec3i) -> T): List<T> =
        (min.x..max.x).flatMap { x ->
            (min.y..max.y).flatMap { y ->
                (min.z..max.z).map { z ->
                    action(Vec3i(x, y, z))
                }
            }
        }

    fun InGame.emulateBlockBreak(pos: BlockPos, state: BlockState) {
        state.block.onBreak(world, pos, state, player)
    }

    fun InGame.breakBlockWithRestrictionChecks(pos: BlockPos) {
        if (player.isBlockBreakingRestricted(world, pos, interactionManager.gameMode))
            return

        val state = world.getBlockState(pos)

        val block = state.block

        if (block is OperatorBlock && !player.isCreativeLevelTwoOp) return

        if (state.isAir) return

        block.onBreak(world, pos, state, player)
        val fluidState = world.getFluidState(pos)
        val bl = world.setBlockState(pos, fluidState.blockState, 11)
        if (bl) {
            block.onBroken(world, pos, state)
        }
    }
}

fun BlockPos.closestCorner(toPos: Vec3d) : Vec3d {
    val x = if (toPos.x > x) x.toDouble() + 0.001 else x.toDouble() + 0.999
    val y = if (toPos.y > y) y.toDouble() + 0.001 else y.toDouble() + 0.999
    val z = if (toPos.z > z) z.toDouble() + 0.001 else z.toDouble() + 0.999

    return Vec3d(x, y, z)
}