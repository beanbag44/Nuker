package mc.merge.module.modules.canal.tools

import net.minecraft.block.BlockState

interface IProtectionLocator {
    fun isProtection(x: Int, y: Int, z: Int): Boolean
    fun isCorrectInPosition(x: Int, y: Int, z: Int): Boolean
    fun statesForPosition(x: Int, y: Int, z: Int): List<BlockState?>?
}
