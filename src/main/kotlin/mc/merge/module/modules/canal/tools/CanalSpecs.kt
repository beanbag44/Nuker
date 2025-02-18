package mc.merge.module.modules.canal.tools

import mc.merge.util.BlockUtils.isSignOrBanner
import mc.merge.util.BlockUtils.isSupportingSignOrBanner
import mc.merge.util.InGame
import mc.merge.util.runInGame
import net.minecraft.block.*
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.util.math.BlockPos

@Suppress("MemberVisibilityCanBePrivate")
class CanalSpecs {
    companion object {
        private const val FREEZING_TEMPERATURE = 0.15f

        var protectionLocator: IProtectionLocator = GenericProtectionLocator()
        const val MIN_Y: Int = 59
        const val MAX_Y: Int = 319
        const val MIN_X: Int = -16
        const val MAX_X: Int = 15

        const val FLOOR_Y: Int = 59
        const val FLOOR_MIN_X: Int = -13
        const val FLOOR_MAX_X: Int = 12

        const val WALKWAY_Y: Int = 62
        const val WEST_WALKWAY_MIN_X: Int = -16
        const val WEST_WALKWAY_MAX_X: Int = -14
        const val EAST_WALKWAY_MIN_X: Int = 13
        const val EAST_WALKWAY_MAX_X: Int = 15

        const val WALL_MIN_Y: Int = 60
        const val WALL_MAX_Y: Int = 61
        const val WEST_WALL_X: Int = -14
        const val EAST_WALL_X: Int = 13

        const val CEILING_Y: Int = 356


        fun isInBounds(x: Int, y: Int): Boolean {
            return contains(x, y, MIN_X, MAX_X, MIN_Y, MAX_Y)
        }

        fun isFloor(x: Int, y: Int): Boolean {
            return contains(x, y, FLOOR_MIN_X, FLOOR_MAX_X, FLOOR_Y, FLOOR_Y)
        }

        fun isWalkway(x: Int, y: Int): Boolean {
            return contains(x, y, WEST_WALKWAY_MIN_X, WEST_WALKWAY_MAX_X, WALKWAY_Y, WALKWAY_Y)
                    || contains(x, y, EAST_WALKWAY_MIN_X, EAST_WALKWAY_MAX_X, WALKWAY_Y, WALKWAY_Y)
        }

        fun isWall(x: Int, y: Int): Boolean {
            return contains(x, y, WEST_WALL_X, WEST_WALL_X, WALL_MIN_Y, WALL_MAX_Y)
                    || contains(x, y, EAST_WALL_X, EAST_WALL_X, WALL_MIN_Y, WALL_MAX_Y)
        }

        fun isBasin(x: Int, y: Int): Boolean {
            return isFloor(x, y) || isWalkway(x, y) || isWall(x, y)
        }

        fun isProtection(x: Int, y: Int, z: Int): Boolean {
            return protectionLocator.isProtection(x, y, z)
        }

        fun InGame.isCorrectInPosition(pos: BlockPos): Boolean {
                checkNotNull(MinecraftClient.getInstance().world)

                val x = pos.x
                val y = pos.y
                val z = pos.z

                // Everything outside doesn't matter
                if (!isInBounds(x, y)) return true

                // The 6 blocks below the sidewalk
                if ((x < WEST_WALL_X || x > EAST_WALL_X) && (y >= FLOOR_Y && y <= FLOOR_Y + 2)) return true

                // The one block below the wall
                if ((x == WEST_WALL_X || x == EAST_WALL_X) && y == FLOOR_Y) return true

                val blockState: BlockState = world.getBlockState(pos)

                return if (isBasin(x, y) || y == CEILING_Y || isProtection(x, y, z)) {
                    stateForPosition(pos).block === blockState.block
                } else blockState.block === Blocks.AIR

        }

        fun InGame.stateForPosition(pos: BlockPos): BlockState {
            checkNotNull(MinecraftClient.getInstance().world)
            val x = pos.x
            val y = pos.y
            val z = pos.z

            // Everything outside doesn't matter
            if (!isInBounds(x, y) || (!isBasin(x, y) && y != CEILING_Y && !isProtection(x, y, z))) {
                return world.getBlockState(pos)
            }

            if (isProtection(x, y, z)) {
                return if (protectionLocator.statesForPosition(x, y, z)!!
                        .contains(world.getBlockState(pos))
                ) {
                    world.getBlockState(pos)
                } else {
                    protectionLocator.statesForPosition(x, y, z)!!.first()!!
                }
            }

            if (y == CEILING_Y) {
                val canFreeze = world.getBiome(pos.withY(WALKWAY_Y + 1))
                    .value().temperature < FREEZING_TEMPERATURE
                return if (canFreeze) {
                    if (x == MIN_X || x == MAX_X) {
                        Blocks.CRYING_OBSIDIAN.defaultState
                    } else if (x <= WEST_WALL_X || x >= EAST_WALL_X || (x + z) % 2 == 0) {
                        Blocks.OBSIDIAN.defaultState
                    } else {
                        Blocks.GLASS.defaultState
                    }
                } else {
                    world.getBlockState(pos)
                }
            }
            val isRiver = world.getBiome(pos).isIn(BiomeTags.IS_RIVER)
            val isFloor = isFloor(x, y)
            val isLightSource = x == WEST_WALKWAY_MIN_X || x == EAST_WALKWAY_MAX_X
            return if (isRiver && isFloor || isLightSource) Blocks.CRYING_OBSIDIAN.defaultState else Blocks.OBSIDIAN.defaultState
        }

        fun InGame.canIgnoreForBreak(pos: BlockPos?): Boolean {
            val block: Block = world.getBlockState(pos).block
            return block is LeavesBlock
                    || block is TallPlantBlock
                    || block is FlowerBlock
                    || block is VineBlock
                    || block is NetherPortalBlock
                    || block is TorchBlock
                    || block is SnowBlock
        }

        fun isProtected(pos: BlockPos?): Boolean {
            if (pos != null){
                runInGame {
                    isSignOrBanner(pos) || isSupportingSignOrBanner(pos)
                }
            }
            return false
        }


        private fun contains(x: Int, y: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Boolean {
            return x in minX..maxX && y in minY..maxY
        }

    }


}