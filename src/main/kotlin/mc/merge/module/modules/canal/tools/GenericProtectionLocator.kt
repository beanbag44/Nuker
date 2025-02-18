package mc.merge.module.modules.canal.tools

import mc.merge.ModCore
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos


class GenericProtectionLocator : IProtectionLocator {
    override fun isProtection(x: Int, y: Int, z: Int): Boolean {
        return x in PROTECTION_MIN_X..PROTECTION_MAX_X && (y in UNDERWATER_MIN_Y..UNDERWATER_MAX_Y || y in BRIDGE_MIN_Y..BRIDGE_MAX_Y)
    }

    override fun isCorrectInPosition(x: Int, y: Int, z: Int): Boolean {
        return statesForPosition(x, y, z)!!.map { it.block }.toList().contains(
            ModCore.mc.world?.getBlockState(BlockPos(x, y, z))?.block
        )
    }

    override fun statesForPosition(x: Int, y: Int, z: Int): List<BlockState>? {
        if (!isProtection(x, y, z)) return listOf()
        when (y) {
            UNDERWATER_MIN_Y -> {
                return listOf(Blocks.OBSIDIAN.defaultState)
            }
            UNDERWATER_MAX_Y -> {
                return listOf(Blocks.OBSIDIAN.defaultState)
            }
            BRIDGE_MIN_Y -> {
                return if (x > CanalSpecs.WEST_WALL_X + 2 && x < CanalSpecs.EAST_WALL_X - 2) {
                    listOf()
                } else {
                    listOf(Blocks.AIR.defaultState)
                }
            }
            BRIDGE_MIN_Y + 1 -> {
                return if (x == PROTECTION_MIN_X + 1 || x == PROTECTION_MIN_X + 2 || x == PROTECTION_MAX_X - 1 || x == PROTECTION_MAX_X - 2) {
                    listOf(Blocks.OBSIDIAN.defaultState)
                } else {
                    listOf(Blocks.AIR.defaultState)
                }
            }
            BRIDGE_MIN_Y + 2 -> {
                return if (x <= PROTECTION_MAX_X - 1 && x >= PROTECTION_MIN_X + 1) {
                    if (x == PROTECTION_MAX_X - 2 || x == PROTECTION_MIN_X + 2) {
                        listOf(Blocks.OBSIDIAN.defaultState)
                    } else {
                        listOf(
                            Blocks.OBSIDIAN.defaultState,
                            Blocks.CRYING_OBSIDIAN.defaultState,
                            Blocks.COBBLESTONE.defaultState
                        )
                    }
                } else {
                    listOf(Blocks.AIR.defaultState)
                }
            }
            BRIDGE_MIN_Y + 3 -> {
                return if (x <= PROTECTION_MAX_X - 2 && x >= PROTECTION_MIN_X + 2) {
                    if (x == PROTECTION_MAX_X - 3 || x == PROTECTION_MIN_X + 3) {
                        listOf(Blocks.OBSIDIAN.defaultState)
                    } else {
                        listOf(Blocks.OBSIDIAN.defaultState, Blocks.CRYING_OBSIDIAN.defaultState)
                    }
                } else {
                    listOf(Blocks.AIR.defaultState)
                }
            }
            else -> return null
        }
    }

    companion object {
        const val UNDERWATER_MAX_Y: Int = CanalSpecs.WALKWAY_Y - 1
        const val UNDERWATER_MIN_Y: Int = CanalSpecs.FLOOR_Y + 1
        const val PROTECTION_MIN_X: Int = CanalSpecs.WEST_WALL_X + 1
        const val PROTECTION_MAX_X: Int = CanalSpecs.EAST_WALL_X - 1

        const val BRIDGE_MIN_Y: Int = CanalSpecs.WALKWAY_Y
        const val BRIDGE_MAX_Y: Int = CanalSpecs.WALKWAY_Y + 3
    }
}
