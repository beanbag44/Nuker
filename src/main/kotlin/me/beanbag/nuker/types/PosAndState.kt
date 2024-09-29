package me.beanbag.nuker.types

import net.minecraft.block.BlockState
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

data class PosAndState(val blockPos: BlockPos, val blockState: BlockState) {
    companion object{
        fun from(blockPos: BlockPos, world: ClientWorld) = PosAndState(blockPos, world.getBlockState(blockPos))
    }
}