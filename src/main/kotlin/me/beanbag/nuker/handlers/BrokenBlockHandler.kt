package me.beanbag.nuker.handlers

import net.minecraft.util.math.BlockPos

object BrokenBlockHandler {
    private val soundQueue = hashMapOf<Long, BlockPos>()

    fun updateSoundQueue() {
        soundQueue.keys.removeIf { timeAdded ->
            System.currentTimeMillis() - timeAdded > 2000
        }
    }
}