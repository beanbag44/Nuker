package me.beanbag.nuker.utils

import me.beanbag.nuker.ModConfigs.mc
import net.minecraft.block.Blocks
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.ceil

object PlaceUtils {
    fun positionsToPlaceBlockFrom(placePos: BlockPos, reach: Double, requireRayCast: Boolean): List<BlockPos> {
        val sidesToPlaceAgainst: MutableList<Direction> = mutableListOf()
        for (side in Direction.entries) {
            val adjacentBlock = mc.world?.getBlockState(placePos.offset(side))

            if (adjacentBlock?.isFullCube(mc.world, placePos.offset(side)) == true) {
                sidesToPlaceAgainst.add(side)
            }
        }

        val placeFromPositions: MutableSet<BlockPos> = mutableSetOf()

        val rangeToCheck = ceil(reach + 1).toInt()

        for (side in sidesToPlaceAgainst) {
            for (x in (placePos.x - rangeToCheck)..(placePos.x + rangeToCheck)) {
                if (side == Direction.EAST && x >= placePos.x) continue
                if (side == Direction.WEST && x <= placePos.x) continue
                for (y in (placePos.y - rangeToCheck)..(placePos.y + rangeToCheck)) {
                    for (z in (placePos.z - rangeToCheck)..(placePos.z + rangeToCheck)) {
                        if (side == Direction.SOUTH && z > placePos.z) continue
                        if (side == Direction.NORTH && z < placePos.z) continue

                        val placeAgainst = placePos.offset(side)
                        val placeAgainstSide = side.opposite

                        val standPos = BlockPos(x, y, z)
                        if (standPos == placePos) continue
                        val playerEyePos = standPos.toCenterPos().add(0.0, 1.12, 0.0)
                        val playerSneakingEyePos = standPos.toCenterPos().add(0.0, 1.0, 0.0)
                        val blockBelow = mc.world?.getBlockState(standPos.down())
                        if (mc.world?.getBlockState(standPos)?.isAir == true
                            && mc.world?.getBlockState(standPos.up())?.isAir == true
                            && (blockBelow?.isFullCube(mc.world, standPos.down()) == true || blockBelow == Blocks.WATER) //has supporting block below
                            && (!requireRayCast
                                    || blockHitsForFace(
                                playerEyePos,
                                placeAgainst,
                                placeAgainstSide,
                                reach
                            ).isNotEmpty()
                                    || blockHitsForFace(
                                playerSneakingEyePos,
                                placeAgainst,
                                placeAgainstSide,
                                reach
                            ).isNotEmpty()) //can raytrace if required
                            && pointsToCheckOnFace(placeAgainst, placeAgainstSide).any { it.distanceTo(playerEyePos) <= reach } //player head will be the right distance away //TODO:
                        ) {
                            placeFromPositions.add(standPos)
                        }
                    }
                }
            }
        }

        return placeFromPositions.toList()
    }

    fun blockHitsForFace(
        fromPos: Vec3d,
        blockPos: BlockPos,
        face: Direction,
        maxDistance: Double
    ): List<BlockHitResult> {
        val placesToCheck = pointsToCheckOnFace(blockPos, face)

        return placesToCheck.map { point ->
            if (fromPos.distanceTo(point) > maxDistance) return@map null
            val hit = mc.world?.raycast(
                RaycastContext(
                    fromPos,
                    point,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
                )
            )
            return@map hit
        }.filterNotNull()
            .filter { hit -> hit.type != HitResult.Type.MISS && hit.blockPos == blockPos && hit.side == face }
    }

    fun pointsToCheckOnFace(blockPos: BlockPos, face: Direction): List<Vec3d> {
        val blockCenter = blockPos.toCenterPos()

        val isX = face.vector.x != 0
        val isY = face.vector.y != 0
        val isZ = face.vector.z != 0

        //get the min and max values for each axis, but snap to the face for the direction we are checking
        val toBlockEdge = 0.499999
        val closeToEdge = 0.45

        val xMax = blockCenter.x + if (!isX) closeToEdge else face.vector.x.toDouble() * toBlockEdge
        val xCenter = blockCenter.x + if (!isX) 0.0 else face.vector.x.toDouble() * toBlockEdge
        val xMin = blockCenter.x + if (!isX) -closeToEdge else face.vector.x.toDouble() * toBlockEdge

        val yMax = blockCenter.y + if (!isY) closeToEdge else face.vector.y.toDouble() * toBlockEdge
        val yCenter = blockCenter.y + if (!isY) 0.0 else face.vector.y.toDouble() * toBlockEdge
        val yMin = blockCenter.y + if (!isY) -closeToEdge else face.vector.y.toDouble() * toBlockEdge

        val zMax = blockCenter.z + if (!isZ) closeToEdge else face.vector.z.toDouble() * toBlockEdge
        val zCenter = blockCenter.z + if (!isZ) 0.0 else face.vector.z.toDouble() * toBlockEdge
        val zMin = blockCenter.z + if (!isZ) -closeToEdge else face.vector.z.toDouble() * toBlockEdge

        val placesToCheck = setOf(
            //points just inside the middle of each edge, but snapped to the target face,
            // Example Dots and Faces:
            //     --.--
            //   /     / |
            //  .     . *|
            // /__.__/  /
            // |.   .| .
            // |__.__|/
            // When all points are snapped to the face, it will give a point just inside
            // each corner and just inside the middle of each side
            // after:
            // -.-.-.-
            // |. . .|
            // |._._.|
            Vec3d(xCenter, yMax, zMin),
            Vec3d(xCenter, yMax, zMax),
            Vec3d(xCenter, yMin, zMin),
            Vec3d(xCenter, yMin, zMax),
            Vec3d(xMin, yCenter, zMin),
            Vec3d(xMin, yCenter, zMax),
            Vec3d(xMax, yCenter, zMin),
            Vec3d(xMax, yCenter, zMax),
            Vec3d(xMin, yMin, zCenter),
            Vec3d(xMin, yMax, zCenter),
            Vec3d(xMax, yMin, zCenter),
            Vec3d(xMax, yMax, zCenter),
            //center
            blockCenter.add(
                face.vector.x.toDouble() * 0.5,
                face.vector.y.toDouble() * 0.5,
                face.vector.z.toDouble() * 0.5
            )
        )
        return placesToCheck.toList()
    }
}