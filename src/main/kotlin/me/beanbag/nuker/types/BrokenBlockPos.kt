package me.beanbag.nuker.types

import me.beanbag.nuker.modules.Nuker.validateBreak
import me.beanbag.nuker.utils.BlockUtils.state
import net.minecraft.util.math.BlockPos

class BrokenBlockPos(blockPos: BlockPos, var broken: Boolean) : BlockPos(blockPos.x, blockPos.y, blockPos.z) {
    val previousState = if (!validateBreak) blockPos.state else null
}